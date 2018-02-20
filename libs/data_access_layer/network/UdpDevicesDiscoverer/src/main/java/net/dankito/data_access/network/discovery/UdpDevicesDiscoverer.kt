package net.dankito.data_access.network.discovery

import net.dankito.utils.AsyncProducerConsumerQueue
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.network.INetworkConnectivityManager
import net.dankito.utils.services.network.NetworkHelper
import net.dankito.utils.services.network.NetworkInterfaceState
import org.slf4j.LoggerFactory
import java.net.*
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.schedule


// TODO: be aware that this approach can disturb busy local networks. From Wikipedia UPnP article:
// UPnP is generally regarded as unsuitable for deployment in business settings for reasons of economy, complexity, and consistency: the multicast foundation makes it chatty, consuming too many network resources on networks with a large population of devices
open class UdpDevicesDiscoverer(private val networkConnectivityManager: INetworkConnectivityManager, protected var threadPool: IThreadPool) : IDevicesDiscoverer {

    companion object {

        protected val MESSAGE_HEADER_AND_BODY_SEPARATOR = ":"

        protected val MESSAGES_CHARSET = Charset.forName("utf8")

        protected val DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR = " | "

        protected val DELAY_BEFORE_RESTARTING_BROADCAST_FOR_ADDRESS_MILLIS = 5000

        private val log = LoggerFactory.getLogger(UdpDevicesDiscoverer::class.java)
    }


    private var connectionsAliveWatcher: ConnectionsAliveWatcher? = null

    private val networkHelper: NetworkHelper = NetworkHelper()

    private var listenerThread: Thread? = null

    private var listenerSocket: DatagramSocket? = null
    private var isListenerSocketOpened = false

    private var broadcastThreads: MutableMap<String, Thread> = ConcurrentHashMap()

    private var networkInterfaceConnectivityChangedListener: ((NetworkInterfaceState) -> Unit)? = null

    private var openedBroadcastSockets: MutableList<DatagramSocket> = ArrayList()
    private var areBroadcastSocketsOpened = false

    private var timerToRestartBroadcastForBroadcastAddress: Timer? = null

    private val receivedPacketsQueue: AsyncProducerConsumerQueue<ReceivedUdpDevicesDiscovererPacket>

    private var foundDevices: MutableList<String> = CopyOnWriteArrayList()


    init {
        receivedPacketsQueue = AsyncProducerConsumerQueue(3, autoStart = false) { receivedPacket ->
            handleReceivedPacket(receivedPacket.receivedData, receivedPacket.senderAddress, receivedPacket.localDeviceInfo,
                    receivedPacket.discoveryMessagePrefix, receivedPacket.listener)
        }
    }


    override val isRunning: Boolean
        get() = isListenerSocketOpened && areBroadcastSocketsOpened

    override fun startAsync(config: DevicesDiscovererConfig) {
        log.info("Starting UdpDevicesDiscoverer " + config.localDeviceInfo + " ...")

        receivedPacketsQueue.start()

        // * 3.5 = from 3 messages one must be received to be still valued as 'connected'
        this.connectionsAliveWatcher = ConnectionsAliveWatcher((config.checkForDevicesIntervalMillis * 3.5).toInt())

        startListenerAsync(config)

        startBroadcastAsync(config)
    }

    override fun stop() {
        log.info("Stopping UdpDevicesDiscoverer ...")

        receivedPacketsQueue.stopAndClearQueue()

        connectionsAliveWatcher?.stopWatching()

        stopListener()

        stopBroadcast()
    }

    private fun stopBroadcast() {
        synchronized(broadcastThreads) {
            networkInterfaceConnectivityChangedListener?.let { networkConnectivityManager.removeNetworkInterfaceConnectivityChangedListener(it) }

            areBroadcastSocketsOpened = false

            for (clientSocket in openedBroadcastSockets) {
                clientSocket.close()
            }

            openedBroadcastSockets.clear()

            for (broadcastAddress in ArrayList(broadcastThreads.keys)) {
                stopBroadcast(broadcastAddress)
            }
        }
    }

    private fun stopBroadcast(broadcastAddress: String) {
        broadcastThreads[broadcastAddress]?.let { broadcastThread ->
            try {
                broadcastThread.interrupt()
            } catch (ignored: Exception) {
            }

            broadcastThreads.remove(broadcastAddress)
            log.info("Stopped broadcasting for Address " + broadcastAddress)
        }
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
        removeDeviceFromFoundDevices(deviceInfo)
    }


    private fun startListenerAsync(config: DevicesDiscovererConfig) {
        stopListener()

        listenerThread = Thread({ startListener(config) }, "UdpDevicesDiscoverer_Listener")

        listenerThread?.start()
    }

    private fun stopListener() {
        try { listenerThread?.interrupt() } catch (ignored: Exception) { }
        listenerThread = null

        if (isListenerSocketOpened) {
            listenerSocket?.close()
            listenerSocket = null
            isListenerSocketOpened = false
        }
    }

    private fun startListener(config: DevicesDiscovererConfig) {
        try {
            this.listenerSocket = createListenerSocket(config.discoverDevicesPort)

            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            while(isListenerSocketOpened) {
                try {
                    listenerSocket?.receive(packet)
                } catch (ex: Exception) {
                    if (isSocketCloseException(ex) == true)
                    // communication has been cancelled by close() method
                        break
                    else {
                        log.error("An Error occurred receiving Packets. listenerSocket = " + listenerSocket, ex)
                        startListener(config)
                    }
                }

                listenerReceivedPacket(buffer, packet, config)
            }
        } catch (ex: Exception) {
            log.error("An error occurred starting UdpDevicesSearcher", ex)
        }

    }

    @Throws(SocketException::class)
    private fun createListenerSocket(discoverDevicesPort: Int): DatagramSocket {
        val listenerSocket = DatagramSocket(null) // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
        listenerSocket.reuseAddress = true // and reuseAddress to true
        listenerSocket.bind(InetSocketAddress(discoverDevicesPort))

        listenerSocket.broadcast = true
        isListenerSocketOpened = true

        return listenerSocket
    }

    private fun isSocketCloseException(ex: Exception): Boolean {
        return networkHelper.isSocketCloseException(ex)
    }


    private fun listenerReceivedPacket(buffer: ByteArray, packet: DatagramPacket, config: DevicesDiscovererConfig) {
        receivedPacketsQueue.add(ReceivedUdpDevicesDiscovererPacket(Arrays.copyOf(buffer, packet.length), packet, packet.address.hostAddress,
                config.localDeviceInfo, config.discoveryMessagePrefix, config.listener))
    }


    private fun handleReceivedPacket(receivedData: ByteArray, senderAddress: String, localDeviceInfo: String, discoveryMessagePrefix: String, listener: DevicesDiscovererListener) {
        val receivedMessage = parseBytesToString(receivedData, receivedData.size)

        if(isSearchingForDevicesMessage(receivedMessage, discoveryMessagePrefix)) {
            val remoteDeviceInfo = getDeviceInfoFromMessage(receivedMessage)

            if(isSelfSentPacket(remoteDeviceInfo, localDeviceInfo) == false) {
                handleReceivedRemotePacket(remoteDeviceInfo, senderAddress, listener)
            }
        }
    }

    private fun handleReceivedRemotePacket(remoteDeviceInfo: String, senderAddress: String, listener: DevicesDiscovererListener) {
        val remoteDeviceKey = createDeviceKey(senderAddress, remoteDeviceInfo)
        connectionsAliveWatcher?.receivedMessageFromDevice(remoteDeviceKey) // if device just reconnected it's better to update its timestamp immediately

        if(hasDeviceAlreadyBeenFound(remoteDeviceKey) == false) {
            deviceFound(remoteDeviceKey, remoteDeviceInfo, senderAddress, listener)
        }
    }

    private fun isSearchingForDevicesMessage(receivedMessage: String, discoveryMessagePrefix: String): Boolean {
        return receivedMessage.startsWith(discoveryMessagePrefix + MESSAGE_HEADER_AND_BODY_SEPARATOR)
    }

    private fun isSelfSentPacket(remoteDeviceInfo: String, localDeviceInfo: String): Boolean {
        return localDeviceInfo == remoteDeviceInfo
    }

    private fun hasDeviceAlreadyBeenFound(deviceInfo: String): Boolean {
        val foundDevicesCopy = ArrayList(foundDevices)

        for(foundDevice in foundDevicesCopy) {
            if (foundDevice == deviceInfo) {
                return true
            }
        }

        return false
    }

    private fun deviceFound(remoteDeviceKey: String, remoteDeviceInfo: String, remoteDeviceAddress: String, listener: DevicesDiscovererListener) {
        log.info("Found Device $remoteDeviceInfo on $remoteDeviceAddress")

        synchronized(this) {
            foundDevices.add(remoteDeviceKey)

            if(foundDevices.size == 1) {
                startConnectionsAliveWatcher(listener)
            }
        }

        listener.deviceFound(remoteDeviceInfo, remoteDeviceAddress)
    }

    private fun startConnectionsAliveWatcher(listener: DevicesDiscovererListener) {
        connectionsAliveWatcher?.startWatchingAsync(foundDevices, object : ConnectionsAliveWatcherListener {
            override fun deviceDisconnected(deviceInfo: String) {
                this@UdpDevicesDiscoverer.deviceDisconnected(deviceInfo, listener)
            }
        })
    }

    protected fun deviceDisconnected(deviceKey: String, listener: DevicesDiscovererListener?) {
        removeDeviceFromFoundDevices(deviceKey)

        extractDeviceInfoFromDeviceInfoKey(deviceKey)?.let { deviceInfo ->
            listener?.deviceDisconnected(deviceInfo)
        }
    }


    private fun startBroadcastAsync(config: DevicesDiscovererConfig) {
        threadPool.runAsync { startBroadcast(config) }
    }

    private fun startBroadcast(config: DevicesDiscovererConfig) {
        networkInterfaceConnectivityChangedListener = { networkInterfaceConnectivityChanged(it, config) }

        networkInterfaceConnectivityChangedListener?.let { networkConnectivityManager.addNetworkInterfaceConnectivityChangedListener(it) }

        for (broadcastAddress in networkConnectivityManager.getBroadcastAddresses()) {
            startBroadcastForBroadcastAddressAsync(broadcastAddress, config)
        }
    }

    private fun networkInterfaceConnectivityChanged(state: NetworkInterfaceState, config: DevicesDiscovererConfig) {
        synchronized(broadcastThreads) {
            state.broadcastAddress?.hostAddress?.let { broadcastAddress ->
                if(broadcastThreads.containsKey(broadcastAddress)) {
                    if(state.isUp == false) {
                        stopBroadcast(broadcastAddress)
                    }
                }
                else if(state.isUp) {
                    state.broadcastAddress?.let { startBroadcastForBroadcastAddressAsync(it, config) }
                }
            }
        }
    }

    private fun startBroadcastForBroadcastAddressAsync(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        synchronized(broadcastThreads) {
            val broadcastThread = Thread(Runnable { startBroadcastForBroadcastAddress(broadcastAddress, config) }, "UdpDevicesDiscoverer_BroadcastTo_" + broadcastAddress.hostAddress)

            broadcastThreads.put(broadcastAddress.hostAddress, broadcastThread)

            broadcastThread.start()
        }
    }

    protected fun startBroadcastForBroadcastAddress(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        try {
            log.info("Starting broadcast for address ${broadcastAddress.hostAddress}")
            val broadcastSocket = DatagramSocket()

            synchronized(broadcastThreads) {
                openedBroadcastSockets.add(broadcastSocket)
                areBroadcastSocketsOpened = true
            }

            broadcastSocket.soTimeout = 10000

            val timer = Timer()
            timer.schedule(config.checkForDevicesIntervalMillis.toLong(), config.checkForDevicesIntervalMillis.toLong()) {
                if(broadcastSocket.isClosed || sendBroadcastOnSocket(broadcastSocket, broadcastAddress, config) == false) {
                    timer.cancel()
                }
            }
        } catch (e: Exception) {
            log.error("Could not start broadcast for address ${broadcastAddress.hostAddress}", e)
        }

    }

    private fun sendBroadcastOnSocket(broadcastSocket: DatagramSocket, broadcastAddress: InetAddress, config: DevicesDiscovererConfig): Boolean {
        try {
            val searchDevicesPacket = createSearchDevicesDatagramPacket(broadcastAddress, config)
            broadcastSocket.send(searchDevicesPacket)
        } catch (e: Exception) {
            log.error("Could not send Broadcast to Address " + broadcastAddress, e)

            synchronized(broadcastThreads) {
                openedBroadcastSockets.remove(broadcastSocket)
            }
            broadcastSocket.close()

            restartBroadcastForBroadcastAddress(broadcastAddress, config)

            return false
        }

        return true
    }

    private fun restartBroadcastForBroadcastAddress(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        if (timerToRestartBroadcastForBroadcastAddress == null) {
            timerToRestartBroadcastForBroadcastAddress = Timer(true)
        }

        // TODO: a problem about using Timer is, that then broadcasts are send on Timer thread and not on broadcastThread
        timerToRestartBroadcastForBroadcastAddress?.schedule(object : TimerTask() {
            override fun run() {
                startBroadcastForBroadcastAddress(broadcastAddress, config)
            }
        }, DELAY_BEFORE_RESTARTING_BROADCAST_FOR_ADDRESS_MILLIS.toLong())
    }

    private fun createSearchDevicesDatagramPacket(broadcastAddress: InetAddress, config: DevicesDiscovererConfig): DatagramPacket {
        val message = config.discoveryMessagePrefix + MESSAGE_HEADER_AND_BODY_SEPARATOR + config.localDeviceInfo
        val messageBytes = message.toByteArray(MESSAGES_CHARSET)

        return DatagramPacket(messageBytes, messageBytes.size, broadcastAddress, config.discoverDevicesPort)
    }

    private fun getDeviceInfoFromMessage(receivedMessage: String): String {
        val bodyStartIndex = receivedMessage.indexOf(MESSAGE_HEADER_AND_BODY_SEPARATOR) + MESSAGE_HEADER_AND_BODY_SEPARATOR.length

        return receivedMessage.substring(bodyStartIndex)
    }

    private fun parseBytesToString(receivedData: ByteArray, packetLength: Int): String {
        return String(receivedData, 0, packetLength, MESSAGES_CHARSET)
    }


    private fun removeDeviceFromFoundDevices(deviceInfo: String) {
        foundDevices.remove(deviceInfo)
    }

    private fun createDeviceKey(senderAddress: String, remoteDeviceInfo: String): String {
        return senderAddress + DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR + remoteDeviceInfo
    }

    private fun extractDeviceInfoFromDeviceInfoKey(deviceKey: String): String? {
        var deviceInfoStartIndex = deviceKey.indexOf(DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR)

        if (deviceInfoStartIndex > 0) {
            deviceInfoStartIndex += DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR.length

            return deviceKey.substring(deviceInfoStartIndex)
        }

        return null
    }

}

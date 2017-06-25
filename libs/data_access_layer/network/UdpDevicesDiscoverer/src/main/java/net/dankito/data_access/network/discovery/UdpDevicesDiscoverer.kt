package net.dankito.data_access.network.discovery

import net.dankito.utils.AsyncProducerConsumerQueue
import net.dankito.utils.ConsumerListener
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.network.NetworkHelper
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.*
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


open class UdpDevicesDiscoverer(protected var threadPool: IThreadPool) : IDevicesDiscoverer {

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

    private var openedBroadcastSockets: MutableList<DatagramSocket> = ArrayList()
    private var areBroadcastSocketsOpened = false

    private var timerToRestartBroadcastForBroadcastAddress: Timer? = null

    private val receivedPacketsQueue: AsyncProducerConsumerQueue<ReceivedUdpDevicesDiscovererPacket>

    private var foundDevices: MutableList<String> = CopyOnWriteArrayList()


    protected var receivedPacketsHandler: ConsumerListener<ReceivedUdpDevicesDiscovererPacket> = object : ConsumerListener<ReceivedUdpDevicesDiscovererPacket> {
        override fun consumeItem(receivedPacket: ReceivedUdpDevicesDiscovererPacket) {
            handleReceivedPacket(receivedPacket.receivedData, receivedPacket.senderAddress, receivedPacket.localDeviceInfo,
                    receivedPacket.discoveryMessagePrefix, receivedPacket.listener)
        }
    }


    init {
        receivedPacketsQueue = AsyncProducerConsumerQueue(3, receivedPacketsHandler)
    }


    override val isRunning: Boolean
        get() = isListenerSocketOpened && areBroadcastSocketsOpened

    override fun startAsync(config: DevicesDiscovererConfig) {
        log.info("Starting UdpDevicesDiscoverer " + config.localDeviceInfo + " ...")

        // * 3.5 = from 3 messages one must be received to be still valued as 'connected'
        this.connectionsAliveWatcher = ConnectionsAliveWatcher((config.checkForDevicesIntervalMillis * 10.5).toInt())

        startListenerAsync(config)

        startBroadcastAsync(config)
    }

    override fun stop() {
        log.info("Stopping UdpDevicesDiscoverer ...")

        receivedPacketsQueue.stop()

        connectionsAliveWatcher?.stopWatching()

        stopListener()

        stopBroadcast()
    }

    protected fun stopBroadcast() {
        synchronized(broadcastThreads) {
            areBroadcastSocketsOpened = false

            for (clientSocket in openedBroadcastSockets) {
                clientSocket.close()
            }

            openedBroadcastSockets.clear()

            for (broadcastAddress in ArrayList(broadcastThreads.keys)) {
                broadcastThreads[broadcastAddress]?.let { broadcastThread ->
                    try {
                        broadcastThread.join(100)
                    } catch (ignored: Exception) {
                    }

                    broadcastThreads.remove(broadcastAddress)
                    log.info("Stopped broadcasting for Address " + broadcastAddress)
                }
            }
        }
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
        removeDeviceFromFoundDevices(deviceInfo)
    }


    protected fun startListenerAsync(config: DevicesDiscovererConfig) {
        stopListener()

        listenerThread = Thread({ startListener(config) }, "UdpDevicesDiscoverer_Listener")

        listenerThread?.start()
    }

    protected fun stopListener() {
        try {
            listenerThread?.join(100)
        } catch (ignored: Exception) { }

        listenerThread = null

        if (isListenerSocketOpened) {
            listenerSocket?.close()
            listenerSocket = null
            isListenerSocketOpened = false
        }
    }

    protected fun startListener(config: DevicesDiscovererConfig) {
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
    protected fun createListenerSocket(discoverDevicesPort: Int): DatagramSocket {
        val listenerSocket = DatagramSocket(null) // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
        listenerSocket.reuseAddress = true // and reuseAddress to true
        listenerSocket.bind(InetSocketAddress(discoverDevicesPort))

        listenerSocket.broadcast = true
        isListenerSocketOpened = true

        return listenerSocket
    }

    protected fun isSocketCloseException(ex: Exception): Boolean {
        return networkHelper.isSocketCloseException(ex) ?: false
    }


    protected fun listenerReceivedPacket(buffer: ByteArray, packet: DatagramPacket, config: DevicesDiscovererConfig) {
        receivedPacketsQueue.add(ReceivedUdpDevicesDiscovererPacket(Arrays.copyOf(buffer, packet.length), packet, packet.address.hostAddress,
                config.localDeviceInfo, config.discoveryMessagePrefix, config.listener))
    }


    protected fun handleReceivedPacket(receivedData: ByteArray, senderAddress: String, localDeviceInfo: String, discoveryMessagePrefix: String, listener: DevicesDiscovererListener) {
        val receivedMessage = parseBytesToString(receivedData, receivedData.size)

        if (isSearchingForDevicesMessage(receivedMessage, discoveryMessagePrefix)) {
            val remoteDeviceInfo = getDeviceInfoFromMessage(receivedMessage)

            if (isSelfSentPacket(remoteDeviceInfo, localDeviceInfo) == false) {
                handleReceivedRemotePacket(remoteDeviceInfo, senderAddress, listener)
            }
        }
    }

    protected fun handleReceivedRemotePacket(remoteDeviceInfo: String, senderAddress: String, listener: DevicesDiscovererListener) {
        val remoteDeviceKey = createDeviceKey(senderAddress, remoteDeviceInfo)

        if (hasDeviceAlreadyBeenFound(remoteDeviceKey) == false) {
            deviceFound(remoteDeviceKey, remoteDeviceInfo, senderAddress, listener)
        } else {
            connectionsAliveWatcher?.receivedMessageFromDevice(remoteDeviceKey)
        }
    }

    protected fun isSearchingForDevicesMessage(receivedMessage: String, discoveryMessagePrefix: String): Boolean {
        return receivedMessage.startsWith(discoveryMessagePrefix)
    }

    protected fun isSelfSentPacket(remoteDeviceInfo: String, localDeviceInfo: String): Boolean {
        return localDeviceInfo == remoteDeviceInfo
    }

    protected fun hasDeviceAlreadyBeenFound(deviceInfo: String): Boolean {
        val foundDevicesCopy = ArrayList(foundDevices)

        for (foundDevice in foundDevicesCopy) {
            if (foundDevice == deviceInfo) {
                return true
            }
        }

        return false
    }

    protected fun deviceFound(remoteDeviceKey: String, remoteDeviceInfo: String, remoteDeviceAddress: String, listener: DevicesDiscovererListener) {
        log.info("Found Device $remoteDeviceInfo on $remoteDeviceAddress")

        synchronized(this) {
            foundDevices.add(remoteDeviceKey)

            if (foundDevices.size == 1) {
                startConnectionsAliveWatcher(listener)
            }
        }

        listener.deviceFound(remoteDeviceInfo, remoteDeviceAddress)
    }

    protected fun startConnectionsAliveWatcher(listener: DevicesDiscovererListener) {
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


    protected fun startBroadcastAsync(config: DevicesDiscovererConfig) {
        threadPool.runAsync { startBroadcast(config) }
    }

    protected fun startBroadcast(config: DevicesDiscovererConfig) {
        for (broadcastAddress in networkHelper.broadcastAddresses) {
            startBroadcastForBroadcastAddressAsync(broadcastAddress, config)
        }
    }

    protected fun startBroadcastForBroadcastAddressAsync(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        synchronized(broadcastThreads) {
            val broadcastThread = Thread(Runnable { startBroadcastForBroadcastAddress(broadcastAddress, config) }, "UdpDevicesDiscoverer_BroadcastTo_" + broadcastAddress.hostAddress)

            broadcastThreads.put(broadcastAddress.hostAddress, broadcastThread)

            broadcastThread.start()
        }
    }

    protected fun startBroadcastForBroadcastAddress(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        try {
            val broadcastSocket = DatagramSocket()

            synchronized(broadcastThreads) {
                openedBroadcastSockets.add(broadcastSocket)
                areBroadcastSocketsOpened = true
            }

            broadcastSocket.soTimeout = 10000

            while (broadcastSocket.isClosed == false) {
                try {
                    sendBroadcastOnSocket(broadcastSocket, broadcastAddress, config)
                } catch (e: Exception) {
                    log.error("Could not send Broadcast to Address " + broadcastAddress, e)

                    synchronized(broadcastThreads) {
                        openedBroadcastSockets.remove(broadcastSocket)
                    }
                    broadcastSocket.close()

                    restartBroadcastForBroadcastAddress(broadcastAddress, config)

                    break
                }

            }
        } catch (ex: Exception) {
            log.error("An error occurred trying to find Devices", ex)
        }

    }

    @Throws(IOException::class)
    protected fun sendBroadcastOnSocket(broadcastSocket: DatagramSocket, broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        val searchDevicesPacket = createSearchDevicesDatagramPacket(broadcastAddress, config)
        broadcastSocket.send(searchDevicesPacket)

        try {
            Thread.sleep(config.checkForDevicesIntervalMillis.toLong())
        } catch (ignored: Exception) {
        }

    }

    protected fun restartBroadcastForBroadcastAddress(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
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

    protected fun createSearchDevicesDatagramPacket(broadcastAddress: InetAddress, config: DevicesDiscovererConfig): DatagramPacket {
        val message = config.discoveryMessagePrefix + MESSAGE_HEADER_AND_BODY_SEPARATOR + config.localDeviceInfo
        val messageBytes = message.toByteArray(MESSAGES_CHARSET)

        return DatagramPacket(messageBytes, messageBytes.size, broadcastAddress, config.discoverDevicesPort)
    }

    protected fun getDeviceInfoFromMessage(receivedMessage: String): String {
        val bodyStartIndex = receivedMessage.indexOf(MESSAGE_HEADER_AND_BODY_SEPARATOR) + MESSAGE_HEADER_AND_BODY_SEPARATOR.length

        return receivedMessage.substring(bodyStartIndex)
    }

    protected fun parseBytesToString(receivedData: ByteArray, packetLength: Int): String {
        return String(receivedData, 0, packetLength, MESSAGES_CHARSET)
    }


    protected fun removeDeviceFromFoundDevices(deviceInfo: String) {
        foundDevices.remove(deviceInfo)
    }

    protected fun createDeviceKey(senderAddress: String, remoteDeviceInfo: String): String {
        return senderAddress + DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR + remoteDeviceInfo
    }

    protected fun extractDeviceInfoFromDeviceInfoKey(deviceKey: String): String? {
        var deviceInfoStartIndex = deviceKey.indexOf(DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR)

        if (deviceInfoStartIndex > 0) {
            deviceInfoStartIndex += DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR.length

            return deviceKey.substring(deviceInfoStartIndex)
        }

        return null
    }

}

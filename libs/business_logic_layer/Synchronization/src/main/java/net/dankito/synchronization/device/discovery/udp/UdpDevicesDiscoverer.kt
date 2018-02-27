package net.dankito.synchronization.device.discovery.udp

import net.dankito.synchronization.device.discovery.DevicesDiscovererConfig
import net.dankito.synchronization.device.discovery.DevicesDiscovererListener
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
import net.dankito.util.AsyncProducerConsumerQueue
import net.dankito.util.IThreadPool
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.network.NetworkHelper
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


// TODO: separate in Listener and Broadcast sender?

// TODO: be aware that this approach can disturb busy local networks. From Wikipedia UPnP article:
// UPnP is generally regarded as unsuitable for deployment in business settings for reasons of economy, complexity, and consistency: the multicast foundation makes it chatty, consuming too many network resources on networks with a large population of devices
open class UdpDevicesDiscoverer(protected val networkConnectivityManager: INetworkConnectivityManager, protected var threadPool: IThreadPool) : IDevicesDiscoverer {

    companion object {

        const val MESSAGE_HEADER_AND_BODY_SEPARATOR = ":"

        val MESSAGES_CHARSET = Charset.forName("utf8")

        const val DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR = " | "

        const val DELAY_BEFORE_RESTARTING_BROADCAST_FOR_ADDRESS_MILLIS = 5000

        private val log = LoggerFactory.getLogger(UdpDevicesDiscoverer::class.java)
    }


    protected var broadcaster: UdpBroadcaster? = null

    protected var connectionsAliveWatcher: ConnectionsAliveWatcher? = null

    protected val networkHelper: NetworkHelper = NetworkHelper() // TODO: make configurable which NetworkHelper instance to use

    protected var listenerThread: Thread? = null

    protected var listenerSocket: DatagramSocket? = null
    protected var isListenerSocketOpened = false

    protected val receivedPacketsQueue: AsyncProducerConsumerQueue<ReceivedUdpDevicesDiscovererPacket>

    protected var foundDevices: MutableList<String> = CopyOnWriteArrayList()


    init {
        receivedPacketsQueue = AsyncProducerConsumerQueue(3, autoStart = false) { receivedPacket ->
            handleReceivedPacket(receivedPacket.receivedData, receivedPacket.senderAddress, receivedPacket.localDeviceInfo,
                    receivedPacket.discoveryMessagePrefix, receivedPacket.listener)
        }
    }


    override val isRunning: Boolean
        get() = isListenerSocketOpened && broadcaster?.areBroadcastSocketsOpened ?: false

    override fun startAsync(config: DevicesDiscovererConfig) {
        log.info("Starting UdpDevicesDiscoverer " + config.localDeviceInfo + " ...")

        receivedPacketsQueue.start()

        this.connectionsAliveWatcher = createConnectionsAliveWatcher(config)

        startListenerAsync(config)

        broadcaster = createBroadcaster()
        broadcaster?.startBroadcastAsync(config)
    }

    protected open fun createConnectionsAliveWatcher(config: DevicesDiscovererConfig): ConnectionsAliveWatcher {
        // * 3.5 = from 3 messages one must be received to be still valued as 'connected'
        return ConnectionsAliveWatcher((config.checkForDevicesIntervalMillis * 3.5).toInt())
    }

    protected open fun createBroadcaster(): UdpBroadcaster {
        return UdpBroadcaster(networkConnectivityManager, threadPool)
    }


    override fun stop() {
        log.info("Stopping UdpDevicesDiscoverer ...")

        receivedPacketsQueue.stopAndClearQueue()

        connectionsAliveWatcher?.stopWatching()

        stopListener()

        broadcaster?.stopBroadcasting()

        broadcaster = null
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
        removeDeviceFromFoundDevices(deviceInfo)
    }


    protected open fun startListenerAsync(config: DevicesDiscovererConfig) {
        stopListener()

        listenerThread = Thread({ startListener(config) }, "UdpDevicesDiscoverer_Listener")

        listenerThread?.start()
    }

    protected open fun stopListener() {
        try { listenerThread?.interrupt() } catch (ignored: Exception) { }
        listenerThread = null

        if (isListenerSocketOpened) {
            listenerSocket?.close()
            listenerSocket = null
            isListenerSocketOpened = false
        }
    }

    protected open fun startListener(config: DevicesDiscovererConfig) {
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
    protected open fun createListenerSocket(discoverDevicesPort: Int): DatagramSocket {
        val listenerSocket = DatagramSocket(null) // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
        listenerSocket.reuseAddress = true // and reuseAddress to true
        listenerSocket.bind(InetSocketAddress(discoverDevicesPort))

        listenerSocket.broadcast = true
        isListenerSocketOpened = true

        return listenerSocket
    }

    protected open fun isSocketCloseException(ex: Exception): Boolean {
        return networkHelper.isSocketCloseException(ex)
    }


    protected open fun listenerReceivedPacket(buffer: ByteArray, packet: DatagramPacket, config: DevicesDiscovererConfig) {
        receivedPacketsQueue.add(ReceivedUdpDevicesDiscovererPacket(Arrays.copyOf(buffer, packet.length), packet, packet.address.hostAddress,
                config.localDeviceInfo, config.discoveryMessagePrefix, config.listener))
    }


    protected open fun handleReceivedPacket(receivedData: ByteArray, senderAddress: String, localDeviceInfo: String, discoveryMessagePrefix: String, listener: DevicesDiscovererListener) {
        val receivedMessage = parseBytesToString(receivedData, receivedData.size)

        if(isSearchingForDevicesMessage(receivedMessage, discoveryMessagePrefix)) {
            val remoteDeviceInfo = getDeviceInfoFromMessage(receivedMessage)

            if(isSelfSentPacket(remoteDeviceInfo, localDeviceInfo) == false) {
                handleReceivedRemotePacket(remoteDeviceInfo, senderAddress, listener)
            }
        }
    }

    protected open fun handleReceivedRemotePacket(remoteDeviceInfo: String, senderAddress: String, listener: DevicesDiscovererListener) {
        val remoteDeviceKey = createDeviceKey(senderAddress, remoteDeviceInfo)
        connectionsAliveWatcher?.receivedMessageFromDevice(remoteDeviceKey) // if device just reconnected it's better to update its timestamp immediately

        if(hasDeviceAlreadyBeenFound(remoteDeviceKey) == false) {
            deviceFound(remoteDeviceKey, remoteDeviceInfo, senderAddress, listener)
        }
    }

    protected open fun isSearchingForDevicesMessage(receivedMessage: String, discoveryMessagePrefix: String): Boolean {
        return receivedMessage.startsWith(discoveryMessagePrefix + MESSAGE_HEADER_AND_BODY_SEPARATOR)
    }

    protected open fun isSelfSentPacket(remoteDeviceInfo: String, localDeviceInfo: String): Boolean {
        return localDeviceInfo == remoteDeviceInfo
    }

    protected open fun hasDeviceAlreadyBeenFound(deviceInfo: String): Boolean {
        val foundDevicesCopy = ArrayList(foundDevices)

        for(foundDevice in foundDevicesCopy) {
            if (foundDevice == deviceInfo) {
                return true
            }
        }

        return false
    }

    protected open fun deviceFound(remoteDeviceKey: String, remoteDeviceInfo: String, remoteDeviceAddress: String, listener: DevicesDiscovererListener) {
        log.info("Found Device $remoteDeviceInfo on $remoteDeviceAddress")

        synchronized(this) {
            foundDevices.add(remoteDeviceKey)

            if(foundDevices.size == 1) {
                startConnectionsAliveWatcher(listener)
            }
        }

        listener.deviceFound(remoteDeviceInfo, remoteDeviceAddress)
    }

    protected open fun startConnectionsAliveWatcher(listener: DevicesDiscovererListener) {
        connectionsAliveWatcher?.startWatchingAsync(foundDevices, object : ConnectionsAliveWatcherListener {
            override fun deviceDisconnected(deviceInfo: String) {
                this@UdpDevicesDiscoverer.deviceDisconnected(deviceInfo, listener)
            }
        })
    }

    protected open fun deviceDisconnected(deviceKey: String, listener: DevicesDiscovererListener?) {
        removeDeviceFromFoundDevices(deviceKey)

        extractDeviceInfoFromDeviceInfoKey(deviceKey)?.let { deviceInfo ->
            listener?.deviceDisconnected(deviceInfo)
        }
    }

    protected open fun getDeviceInfoFromMessage(receivedMessage: String): String {
        val bodyStartIndex = receivedMessage.indexOf(MESSAGE_HEADER_AND_BODY_SEPARATOR) + MESSAGE_HEADER_AND_BODY_SEPARATOR.length

        return receivedMessage.substring(bodyStartIndex)
    }

    protected open fun parseBytesToString(receivedData: ByteArray, packetLength: Int): String {
        return String(receivedData, 0, packetLength, MESSAGES_CHARSET)
    }


    protected open fun removeDeviceFromFoundDevices(deviceInfo: String) {
        foundDevices.remove(deviceInfo)
    }

    protected open fun createDeviceKey(senderAddress: String, remoteDeviceInfo: String): String {
        return senderAddress + DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR + remoteDeviceInfo
    }

    protected open fun extractDeviceInfoFromDeviceInfoKey(deviceKey: String): String? {
        var deviceInfoStartIndex = deviceKey.indexOf(DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR)

        if (deviceInfoStartIndex > 0) {
            deviceInfoStartIndex += DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR.length

            return deviceKey.substring(deviceInfoStartIndex)
        }

        return null
    }

}

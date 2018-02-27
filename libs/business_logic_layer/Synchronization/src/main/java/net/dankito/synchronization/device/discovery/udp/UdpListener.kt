package net.dankito.synchronization.device.discovery.udp

import net.dankito.synchronization.device.discovery.DevicesDiscovererConfig
import net.dankito.synchronization.device.discovery.DevicesDiscovererListener
import net.dankito.util.AsyncProducerConsumerQueue
import net.dankito.util.network.NetworkHelper
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


open class UdpListener(protected val connectionsAliveWatcher: ConnectionsAliveWatcher, protected val networkHelper: NetworkHelper) {

    companion object {
        private val log = LoggerFactory.getLogger(UdpListener::class.java)
    }


    var isListenerSocketOpened = false
        protected set


    protected var listenerThread: Thread? = null

    protected var listenerSocket: DatagramSocket? = null

    protected val receivedPacketsQueue: AsyncProducerConsumerQueue<ReceivedUdpDevicesDiscovererPacket>

    protected var foundDevices: MutableList<String> = CopyOnWriteArrayList()


    init {
        receivedPacketsQueue = AsyncProducerConsumerQueue(3, autoStart = false) { receivedPacket ->
            handleReceivedPacket(receivedPacket.receivedData, receivedPacket.senderAddress, receivedPacket.localDeviceInfo,
                    receivedPacket.discoveryMessagePrefix, receivedPacket.listener)
        }
    }


    open fun stop() {
        receivedPacketsQueue.stopAndClearQueue()

        try { listenerThread?.interrupt() } catch (ignored: Exception) { }
        listenerThread = null

        listenerSocket?.close()
        listenerSocket = null

        isListenerSocketOpened = false
    }


    open fun startAsync(config: DevicesDiscovererConfig) {
        stop()

        receivedPacketsQueue.start()

        startListenerAsync(config)
    }


    protected open fun startListenerAsync(config: DevicesDiscovererConfig) {
        listenerThread = Thread({ startListener(config) }, "UdpDevicesDiscoverer_Listener")

        listenerThread?.start()
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
                    if (isSocketCloseException(ex) == true) // communication has been cancelled by close() method
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
        connectionsAliveWatcher.receivedMessageFromDevice(remoteDeviceKey) // if device just reconnected it's better to update its timestamp immediately

        if(hasDeviceAlreadyBeenFound(remoteDeviceKey) == false) {
            deviceFound(remoteDeviceKey, remoteDeviceInfo, senderAddress, listener)
        }
    }

    protected open fun isSearchingForDevicesMessage(receivedMessage: String, discoveryMessagePrefix: String): Boolean {
        return receivedMessage.startsWith(discoveryMessagePrefix + UdpDevicesDiscoverer.MESSAGE_HEADER_AND_BODY_SEPARATOR)
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
        connectionsAliveWatcher.startWatchingAsync(foundDevices, object : ConnectionsAliveWatcherListener {
            override fun deviceDisconnected(deviceInfo: String) {
                deviceDisconnected(deviceInfo, listener)
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
        val bodyStartIndex = receivedMessage.indexOf(UdpDevicesDiscoverer.MESSAGE_HEADER_AND_BODY_SEPARATOR) + UdpDevicesDiscoverer.MESSAGE_HEADER_AND_BODY_SEPARATOR.length

        return receivedMessage.substring(bodyStartIndex)
    }

    protected open fun parseBytesToString(receivedData: ByteArray, packetLength: Int): String {
        return String(receivedData, 0, packetLength, UdpDevicesDiscoverer.MESSAGES_CHARSET)
    }


    internal open fun removeDeviceFromFoundDevices(deviceInfo: String) {
        foundDevices.remove(deviceInfo)
    }

    protected open fun createDeviceKey(senderAddress: String, remoteDeviceInfo: String): String {
        return senderAddress + UdpDevicesDiscoverer.DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR + remoteDeviceInfo
    }

    protected open fun extractDeviceInfoFromDeviceInfoKey(deviceKey: String): String? {
        var deviceInfoStartIndex = deviceKey.indexOf(UdpDevicesDiscoverer.DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR)

        if(deviceInfoStartIndex > 0) {
            deviceInfoStartIndex += UdpDevicesDiscoverer.DEVICE_KEY_ADDRESS_AND_DEVICE_INFO_SEPARATOR.length

            return deviceKey.substring(deviceInfoStartIndex)
        }

        return null
    }
}
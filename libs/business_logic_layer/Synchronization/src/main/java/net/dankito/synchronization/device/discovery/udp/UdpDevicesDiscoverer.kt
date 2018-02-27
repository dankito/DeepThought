package net.dankito.synchronization.device.discovery.udp

import net.dankito.synchronization.device.discovery.DevicesDiscovererConfig
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
import net.dankito.util.IThreadPool
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.network.NetworkHelper
import org.slf4j.LoggerFactory
import java.nio.charset.Charset


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


    protected var listener: UdpListener? = null

    protected var broadcaster: UdpBroadcaster? = null

    protected var connectionsAliveWatcher: ConnectionsAliveWatcher? = null


    override val isRunning: Boolean
        get() = listener?.isListenerSocketOpened ?: false && broadcaster?.areBroadcastSocketsOpened ?: false

    override fun startAsync(config: DevicesDiscovererConfig) {
        log.info("Starting UdpDevicesDiscoverer " + config.localDeviceInfo + " ...")

        this.connectionsAliveWatcher = createConnectionsAliveWatcher(config)

        connectionsAliveWatcher?.let { connectionsAliveWatcher ->
            listener = createListener(connectionsAliveWatcher, networkConnectivityManager.networkHelper)
        }

        listener?.startAsync(config)

        broadcaster = createBroadcaster()
        broadcaster?.startBroadcastAsync(config)
    }

    protected open fun createConnectionsAliveWatcher(config: DevicesDiscovererConfig): ConnectionsAliveWatcher {
        // * 3.5 = from 3 messages one must be received to be still valued as 'connected'
        return ConnectionsAliveWatcher((config.checkForDevicesIntervalMillis * 3.5).toInt())
    }

    protected open fun createListener(connectionsAliveWatcher: ConnectionsAliveWatcher, networkHelper: NetworkHelper): UdpListener {
        return UdpListener(connectionsAliveWatcher, networkHelper)
    }

    protected open fun createBroadcaster(): UdpBroadcaster {
        return UdpBroadcaster(networkConnectivityManager, threadPool)
    }


    override fun stop() {
        log.info("Stopping UdpDevicesDiscoverer ...")

        listener?.stop()

        listener = null

        connectionsAliveWatcher?.stopWatching()

        broadcaster?.stopBroadcasting()

        broadcaster = null
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
        listener?.removeDeviceFromFoundDevices(deviceInfo)
    }

}

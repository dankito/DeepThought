package net.dankito.synchronization.device.discovery.udp

import net.dankito.synchronization.device.discovery.DevicesDiscovererConfig
import net.dankito.util.IThreadPool
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.network.NetworkInterfaceState
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule


open class UdpBroadcaster(protected val networkConnectivityManager: INetworkConnectivityManager, protected val threadPool: IThreadPool) {

    companion object {
        private val log = LoggerFactory.getLogger(UdpBroadcaster::class.java)
    }


    var areBroadcastSocketsOpened = false
        protected set

    protected var broadcastThreads: MutableMap<String, Thread> = ConcurrentHashMap()

    protected var networkInterfaceConnectivityChangedListener: ((NetworkInterfaceState) -> Unit)? = null

    protected var openedBroadcastSockets: MutableList<DatagramSocket> = ArrayList()

    protected var timerToRestartBroadcastForBroadcastAddress: Timer? = null


    open fun stopBroadcasting() {
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

    protected open fun stopBroadcast(broadcastAddress: String) {
        broadcastThreads[broadcastAddress]?.let { broadcastThread ->
            try {
                broadcastThread.interrupt()
            } catch (ignored: Exception) {
            }

            broadcastThreads.remove(broadcastAddress)
            log.info("Stopped broadcasting for Address " + broadcastAddress)
        }
    }


    open fun startBroadcastAsync(config: DevicesDiscovererConfig) {
        threadPool.runAsync { startBroadcast(config) }
    }

    protected open fun startBroadcast(config: DevicesDiscovererConfig) {
        networkInterfaceConnectivityChangedListener = { networkInterfaceConnectivityChanged(it, config) }

        networkInterfaceConnectivityChangedListener?.let { networkConnectivityManager.addNetworkInterfaceConnectivityChangedListener(it) }

        for (broadcastAddress in networkConnectivityManager.getBroadcastAddresses()) {
            startBroadcastForBroadcastAddressAsync(broadcastAddress, config)
        }
    }

    protected open fun networkInterfaceConnectivityChanged(state: NetworkInterfaceState, config: DevicesDiscovererConfig) {
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

    protected open fun startBroadcastForBroadcastAddressAsync(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        synchronized(broadcastThreads) {
            val broadcastThread = Thread(Runnable { startBroadcastForBroadcastAddress(broadcastAddress, config) }, "UdpDevicesDiscoverer_BroadcastTo_" + broadcastAddress.hostAddress)

            broadcastThreads.put(broadcastAddress.hostAddress, broadcastThread)

            broadcastThread.start()
        }
    }

    protected open fun startBroadcastForBroadcastAddress(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
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

    protected open fun sendBroadcastOnSocket(broadcastSocket: DatagramSocket, broadcastAddress: InetAddress, config: DevicesDiscovererConfig): Boolean {
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

    protected open fun restartBroadcastForBroadcastAddress(broadcastAddress: InetAddress, config: DevicesDiscovererConfig) {
        if(timerToRestartBroadcastForBroadcastAddress == null) {
            timerToRestartBroadcastForBroadcastAddress = Timer(true)
        }

        // TODO: a problem about using Timer is, that then broadcasts are send on Timer thread and not on broadcastThread
        timerToRestartBroadcastForBroadcastAddress?.schedule(object : TimerTask() {
            override fun run() {
                startBroadcastForBroadcastAddress(broadcastAddress, config)
            }
        }, UdpDevicesDiscoverer.DELAY_BEFORE_RESTARTING_BROADCAST_FOR_ADDRESS_MILLIS.toLong())
    }

    protected open fun createSearchDevicesDatagramPacket(broadcastAddress: InetAddress, config: DevicesDiscovererConfig): DatagramPacket {
        val message = config.discoveryMessagePrefix + UdpDevicesDiscoverer.MESSAGE_HEADER_AND_BODY_SEPARATOR + config.localDeviceInfo
        val messageBytes = message.toByteArray(UdpDevicesDiscoverer.MESSAGES_CHARSET)

        return DatagramPacket(messageBytes, messageBytes.size, broadcastAddress, config.discoverDevicesPort)
    }

}
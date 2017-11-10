package net.dankito.data_access.network.discovery

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class ConnectionsAliveWatcher(protected var connectionTimeout: Int) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionsAliveWatcher::class.java)
    }


    protected var connectionsAliveCheckTimer: Timer? = null

    protected var lastMessageReceivedFromDeviceTimestamps: MutableMap<String, Long> = ConcurrentHashMap()


    val isRunning: Boolean
        get() = connectionsAliveCheckTimer != null

    fun startWatchingAsync(foundDevices: List<String>, listener: ConnectionsAliveWatcherListener) {
        synchronized(this) {
            stopWatching()

            log.info("Starting ConnectionsAliveWatcher ...")

            connectionsAliveCheckTimer = Timer("ConnectionsAliveWatcher Timer")
            connectionsAliveCheckTimer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    checkIfConnectedDevicesStillAreConnected(foundDevices, listener)
                }
            }, connectionTimeout.toLong(), connectionTimeout.toLong())
        }
    }

    fun stopWatching() {
        synchronized(this) {
            if (connectionsAliveCheckTimer != null) {
                log.info("Stopping ConnectionsAliveWatcher ...")

                connectionsAliveCheckTimer!!.cancel()
                connectionsAliveCheckTimer = null

                lastMessageReceivedFromDeviceTimestamps.clear()
            }
        }
    }


    fun receivedMessageFromDevice(deviceInfo: String) {
        lastMessageReceivedFromDeviceTimestamps.put(deviceInfo, Date().time)
    }


    protected fun checkIfConnectedDevicesStillAreConnected(foundDevices: List<String>, listener: ConnectionsAliveWatcherListener) {
        val now = Date().time

        for(foundDeviceKey in foundDevices) {
            if(hasDeviceExpired(foundDeviceKey, now)) {
                lastMessageReceivedFromDeviceTimestamps[foundDeviceKey]?.let { timestamp ->
                    log.info("Device $foundDeviceKey has disconnected, last message received at ${Date(timestamp)}, now = ${Date(now)}. " +
                            "Remaining keys = ${lastMessageReceivedFromDeviceTimestamps.keys}")
                }
                deviceDisconnected(foundDeviceKey, listener)
            }
        }
    }

    protected fun hasDeviceExpired(foundDeviceKey: String, now: Long): Boolean {
        val lastMessageReceivedFromDeviceTimestamp = lastMessageReceivedFromDeviceTimestamps[foundDeviceKey]

        if(lastMessageReceivedFromDeviceTimestamp != null) {
            val hasExpired = lastMessageReceivedFromDeviceTimestamp < now - connectionTimeout
            if(hasExpired) {
                if(hasReconnected(foundDeviceKey)) {
                    lastMessageReceivedFromDeviceTimestamps.remove(foundDeviceKey)
                }
                else {
                    return true
                }
            }
        }

        return false
    }

    private fun hasReconnected(deviceKey: String): Boolean {
        val index = deviceKey.lastIndexOf(":") // this is actually bad as it uses knowledge from ConnectedDevicesService.MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR
        if(index > 0) {
            val deviceKeyWithoutBasicDataSyncPort = deviceKey.substring(0, index)

            for(otherDeviceKey in lastMessageReceivedFromDeviceTimestamps.keys) {
                if(otherDeviceKey.startsWith(deviceKeyWithoutBasicDataSyncPort)) { // device just reconnected shortly with different basic data sync port
                    return true
                }
            }
        }

        return false
    }

    protected fun deviceDisconnected(disconnectedDeviceKey: String, listener: ConnectionsAliveWatcherListener?) {
        lastMessageReceivedFromDeviceTimestamps.remove(disconnectedDeviceKey)

        listener?.deviceDisconnected(disconnectedDeviceKey)
    }

}

package net.dankito.synchronization.device.discovery.udp

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap


open class ConnectionsAliveWatcher(protected var connectionTimeout: Int) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionsAliveWatcher::class.java)
    }


    protected var connectionsAliveCheckTimer: Timer? = null

    protected var lastMessageReceivedFromDeviceTimestamps: MutableMap<String, Long> = ConcurrentHashMap()


    val isRunning: Boolean
        get() = connectionsAliveCheckTimer != null

    open fun startWatchingAsync(foundDevices: List<String>, listener: ConnectionsAliveWatcherListener) {
        synchronized(this) {
            stopWatching()

            log.info("Starting ConnectionsAliveWatcher ...")

            connectionsAliveCheckTimer = Timer("ConnectionsAliveWatcher Timer")
            connectionsAliveCheckTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    checkIfConnectedDevicesStillAreConnected(foundDevices, listener)
                }
            }, connectionTimeout.toLong(), connectionTimeout.toLong())
        }
    }

    open fun stopWatching() {
        synchronized(this) {
            connectionsAliveCheckTimer?.let { timer ->
                log.info("Stopping ConnectionsAliveWatcher ...")

                timer.cancel()
                connectionsAliveCheckTimer = null
            }

            lastMessageReceivedFromDeviceTimestamps.clear()
        }
    }


    open fun receivedMessageFromDevice(deviceInfo: String) {
        lastMessageReceivedFromDeviceTimestamps.put(deviceInfo, Date().time)
    }


    protected open fun checkIfConnectedDevicesStillAreConnected(foundDevices: List<String>, listener: ConnectionsAliveWatcherListener) {
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

    protected open fun hasDeviceExpired(foundDeviceKey: String, now: Long): Boolean {
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

    protected open fun hasReconnected(deviceKey: String): Boolean {
        val index = deviceKey.lastIndexOf(":") // this is actually bad as it uses knowledge from ConnectedDevicesService.MESSAGES_PORT_AND_BASIC_DATA_SYNC_PORT_SEPARATOR

        if(index > 0) {
            val deviceKeyWithoutBasicDataSyncPort = deviceKey.substring(0, index)

            for(otherDeviceKey in lastMessageReceivedFromDeviceTimestamps.keys) {
                if(deviceKey != otherDeviceKey && otherDeviceKey.startsWith(deviceKeyWithoutBasicDataSyncPort)) { // device just reconnected shortly with different basic data sync port
                    return true
                }
            }
        }

        return false
    }

    protected open fun deviceDisconnected(disconnectedDeviceKey: String, listener: ConnectionsAliveWatcherListener?) {
        lastMessageReceivedFromDeviceTimestamps.remove(disconnectedDeviceKey)

        listener?.deviceDisconnected(disconnectedDeviceKey)
    }

}

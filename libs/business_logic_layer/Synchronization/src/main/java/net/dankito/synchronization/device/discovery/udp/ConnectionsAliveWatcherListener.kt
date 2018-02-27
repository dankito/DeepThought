package net.dankito.synchronization.device.discovery.udp


interface ConnectionsAliveWatcherListener {

    fun deviceDisconnected(deviceInfo: String)

}

package net.dankito.synchronization.device.discovery


interface DevicesDiscovererListener {

    fun deviceFound(deviceInfo: String, address: String)

    fun deviceDisconnected(deviceInfo: String)

}

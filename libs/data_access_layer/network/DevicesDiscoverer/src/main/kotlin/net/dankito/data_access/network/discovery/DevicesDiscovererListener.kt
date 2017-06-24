package net.dankito.data_access.network.discovery


interface DevicesDiscovererListener {

    fun deviceFound(deviceInfo: String, address: String)

    fun deviceDisconnected(deviceInfo: String)

}

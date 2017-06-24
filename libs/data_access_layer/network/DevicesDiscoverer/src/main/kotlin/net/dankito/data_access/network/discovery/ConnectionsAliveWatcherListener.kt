package net.dankito.data_access.network.discovery


interface ConnectionsAliveWatcherListener {

    fun deviceDisconnected(deviceInfo: String)

}

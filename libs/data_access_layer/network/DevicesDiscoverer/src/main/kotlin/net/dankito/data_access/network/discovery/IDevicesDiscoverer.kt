package net.dankito.data_access.network.discovery


interface IDevicesDiscoverer {

    val isRunning: Boolean

    fun startAsync(config: DevicesDiscovererConfig)

    fun stop()

    fun disconnectedFromDevice(deviceInfo: String)

}

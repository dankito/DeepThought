package net.dankito.synchronization.device.discovery


interface IDevicesDiscoverer {

    val isRunning: Boolean

    fun startAsync(config: DevicesDiscovererConfig)

    fun stop()

    fun disconnectedFromDevice(deviceInfo: String)

}

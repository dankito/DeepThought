package net.dankito.synchronization.device.discovery


class NoOpDevicesDiscoverer : IDevicesDiscoverer {

    override val isRunning = false


    override fun startAsync(config: DevicesDiscovererConfig) {
    }

    override fun stop() {
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
    }

}
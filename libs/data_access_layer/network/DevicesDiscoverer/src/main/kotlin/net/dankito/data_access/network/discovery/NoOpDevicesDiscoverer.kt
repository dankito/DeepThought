package net.dankito.data_access.network.discovery


class NoOpDevicesDiscoverer : IDevicesDiscoverer {

    override val isRunning: Boolean
        get() = false

    override fun startAsync(config: DevicesDiscovererConfig) {
    }

    override fun stop() {
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
    }

}
package net.dankito.deepthought.android.stub

import net.dankito.synchronization.device.discovery.DevicesDiscovererConfig
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer


class NoOpDevicesDiscoverer : IDevicesDiscoverer {

    override val isRunning = false


    override fun startAsync(config: DevicesDiscovererConfig) {
    }

    override fun stop() {
    }

    override fun disconnectedFromDevice(deviceInfo: String) {
    }

}
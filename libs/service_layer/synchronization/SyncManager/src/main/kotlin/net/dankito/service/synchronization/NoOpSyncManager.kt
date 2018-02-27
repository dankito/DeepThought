package net.dankito.service.synchronization

import net.dankito.synchronization.model.Device
import net.dankito.synchronization.model.DiscoveredDevice


class NoOpSyncManager : ISyncManager {

    override fun startAsync(desiredSynchronizationPort: Int, desiredBasicDataSynchronizationPort: Int, alsoUsePullReplication: Boolean, initializedCallback: (Int) -> Unit) {
    }

    override fun syncBasicDataWithDevice(deviceId: String, remoteDeviceAddress: String, basicDataSyncPort: Int, syncDone: (Device) -> Unit) {
    }

    override fun openSynchronizationPort(): Int? {
        return null
    }

    override fun closeSynchronizationPort() {
    }

    override fun startSynchronizationWithDevice(device: DiscoveredDevice) {
    }

    override fun stopSynchronizationWithDevice(device: DiscoveredDevice) {
    }

    override fun stop() {
    }

}
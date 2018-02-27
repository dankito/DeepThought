package net.dankito.service.synchronization

import net.dankito.synchronization.model.Device
import net.dankito.synchronization.model.DiscoveredDevice


interface ISyncManager {

    companion object {
        const val DefaultBasicDataSynchronizationListenerPort = 32790

        const val DefaultSynchronizationListenerPort = 32791

        const val DefaultAlsoUsePullReplication = false
    }


    fun startAsync(desiredSynchronizationPort: Int = DefaultSynchronizationListenerPort, desiredBasicDataSynchronizationPort: Int = DefaultBasicDataSynchronizationListenerPort,
                   alsoUsePullReplication: Boolean = DefaultAlsoUsePullReplication, initializedCallback: (Int) -> Unit)

    fun syncBasicDataWithDevice(deviceId: String, remoteDeviceAddress: String, basicDataSyncPort: Int, syncDone: (Device) -> Unit)

    @Throws(Exception::class)
    fun openSynchronizationPort(): Int?

    fun closeSynchronizationPort()

    @Throws(Exception::class)
    fun startSynchronizationWithDevice(device: DiscoveredDevice)

    fun stopSynchronizationWithDevice(device: DiscoveredDevice)


    fun stop()

}
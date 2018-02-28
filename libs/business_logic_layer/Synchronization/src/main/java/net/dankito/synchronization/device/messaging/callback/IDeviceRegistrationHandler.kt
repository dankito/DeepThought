package net.dankito.synchronization.device.messaging.callback

import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.message.DeviceInfo
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.SyncInfo


interface IDeviceRegistrationHandler {

    /*
     *  To avoid circular dependency circle we cannot pass IMessenger to DeviceRegistrationHandlerBase's constructor, so we pass it as method parameter
     */
    fun showUnknownDeviceDiscovered(messenger: IMessenger, unknownDevice: DiscoveredDevice)

    fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice)

    fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit)

    fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String)

    fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo?


    fun addRequestingToSynchronizeWithRemoteListener(listener: (remoteDevice: DiscoveredDevice) -> Unit)

    fun addNewDeviceRegisteredListener(listener: (remoteDevice: DiscoveredDevice) -> Unit)

    fun addIgnoreDeviceListener(listener: (remoteDevice: DiscoveredDevice) -> Unit)

}

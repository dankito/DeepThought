package net.dankito.synchronization.device.communication.callback

import net.dankito.synchronization.device.communication.IClientCommunicator
import net.dankito.synchronization.device.communication.message.DeviceInfo
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.SyncInfo


interface IDeviceRegistrationHandler {

    /*
     *  To avoid circular dependency circle we cannot pass IClientCommunicator to DeviceRegistrationHandlerBase's constructor, so we pass it as method parameter
     */
    fun showUnknownDeviceDiscovered(clientCommunicator: IClientCommunicator, unknownDevice: DiscoveredDevice)

    fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice)

    fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit)

    fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String)

    fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo?


    fun addRequestingToSynchronizeWithRemoteListener(listener: (remoteDevice: DiscoveredDevice) -> Unit)

    fun addNewDeviceRegisteredListener(listener: (remoteDevice: DiscoveredDevice) -> Unit)

    fun addIgnoreDeviceListener(listener: (remoteDevice: DiscoveredDevice) -> Unit)

}

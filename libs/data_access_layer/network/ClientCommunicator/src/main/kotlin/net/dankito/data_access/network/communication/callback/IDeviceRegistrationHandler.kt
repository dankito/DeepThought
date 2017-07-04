package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.service.synchronization.initialsync.model.SyncInfo


interface IDeviceRegistrationHandler {

    /*
     *  To avoid circular dependency circle we cannot pass IClientCommunicator to DeviceRegistrationHandlerBase's constructor, so we pass it as method parameter
     */
    fun showUnknownDeviceDiscovered(clientCommunicator: IClientCommunicator, unknownDevice: DiscoveredDevice)

    fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice)

    fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit)

    fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String)

    fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo?

}

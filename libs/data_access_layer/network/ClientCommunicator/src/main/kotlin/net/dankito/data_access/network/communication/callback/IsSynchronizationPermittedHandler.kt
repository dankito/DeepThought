package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.message.DeviceInfo


interface IsSynchronizationPermittedHandler {

    fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit)

    fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String)

}

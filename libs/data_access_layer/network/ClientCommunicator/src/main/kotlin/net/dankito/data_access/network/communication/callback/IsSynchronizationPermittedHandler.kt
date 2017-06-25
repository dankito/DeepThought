package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.message.DeviceInfo


interface IsSynchronizationPermittedHandler {

    fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit)

    fun showCorrectResponseToUserNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String)

}

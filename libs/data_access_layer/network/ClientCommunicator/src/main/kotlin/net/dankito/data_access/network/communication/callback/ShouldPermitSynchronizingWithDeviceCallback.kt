package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.message.DeviceInfo


interface ShouldPermitSynchronizingWithDeviceCallback {

    fun done(remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean)

}

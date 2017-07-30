package net.dankito.data_access.network.communication.message

import net.dankito.deepthought.model.Device
import net.dankito.utils.version.Version
import net.dankito.utils.version.Versions


data class DeviceInfo(val id: String, val uniqueDeviceId: String, val appVersion: Version, val dataModelVersion: Int) {

    companion object {
        fun fromDevice(device: Device): DeviceInfo {
            return DeviceInfo(device.id ?: "", device.uniqueDeviceId, Versions.AppVersion, Versions.DataModelVersion)
        }
    }


    private constructor() : this("", "", Version(0, 0), 0) // for Jackson


}

package net.dankito.data_access.network.communication.message

import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.enums.OsType
import net.dankito.util.Version
import net.dankito.utils.version.Versions


// TODO: get rid of Version and Versions
data class DeviceInfo(val id: String, val uniqueDeviceId: String, val name: String, val osType: OsType, val osName: String, val osVersion: String, val description: String,
                      val appVersion: Version, val dataModelVersion: Int) { // TODO: add CommunicationProtocolVersion

    companion object {
        fun fromDevice(device: Device): DeviceInfo {
            return DeviceInfo(device.id ?: "", device.uniqueDeviceId, device.name, device.osType, device.osName, device.osVersion, device.description,
                    Versions.AppVersion, Versions.DataModelVersion)
        }
    }


    constructor() : this("", "", "", OsType.DESKTOP, "", "", "", Version(0, 0), 0) // for Jackson


    override fun toString(): String {
        var description = osName + " " + osVersion

        if (name != null && name!!.length > 0) {
            description = name + " " + description
        }

        return description
    }


}

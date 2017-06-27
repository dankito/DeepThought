package net.dankito.data_access.network.communication.message

import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.enums.OsType


class DeviceInfo(val id: String, val uniqueDeviceId: String, val name: String, val osType: OsType, val osName: String, val osVersion: String, val description: String) {

    companion object {
        fun fromDevice(device: Device): DeviceInfo {
            return DeviceInfo(device.id ?: "", device.uniqueDeviceId, device.name, device.osType, device.osName, device.osVersion, device.description)
        }
    }


    private constructor() : this("", "", "", OsType.DESKTOP, "", "", "") // for Jackson


    override fun toString(): String {
        var description = osName + " " + osVersion

        if (name != null && name!!.length > 0) {
            description = name + " " + description
        }

        return description
    }


}

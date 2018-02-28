package net.dankito.synchronization.device.messaging.message

import net.dankito.synchronization.model.Device
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.synchronization.model.enums.OsType
import net.dankito.util.Version


// TODO: get rid of Version and Versions
data class DeviceInfo(val id: String, val uniqueDeviceId: String, val name: String, val osType: OsType, val osName: String, val osVersion: String, val description: String,
                      val appVersion: Version, val dataModelVersion: Int) { // TODO: add CommunicationProtocolVersion

    companion object {

        fun fromNetworkSettings(networkSettings: NetworkSettings): DeviceInfo {
            return fromDevice(networkSettings.localHostDevice, networkSettings.appVersion, networkSettings.dataModelVersion)
        }

        fun fromDevice(device: Device, appVersion: Version, dataModelVersion: Int): DeviceInfo {
            return DeviceInfo(device.id ?: "", device.uniqueDeviceId, device.name, device.osType, device.osName, device.osVersion, device.description,
                    appVersion, dataModelVersion)
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

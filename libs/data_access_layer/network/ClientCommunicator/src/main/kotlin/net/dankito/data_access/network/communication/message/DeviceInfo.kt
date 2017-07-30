package net.dankito.data_access.network.communication.message

import net.dankito.deepthought.model.Device


data class DeviceInfo(val id: String, val uniqueDeviceId: String) {

    companion object {
        fun fromDevice(device: Device): DeviceInfo {
            return DeviceInfo(device.id ?: "", device.uniqueDeviceId)
        }
    }


    private constructor() : this("", "") // for Jackson


}

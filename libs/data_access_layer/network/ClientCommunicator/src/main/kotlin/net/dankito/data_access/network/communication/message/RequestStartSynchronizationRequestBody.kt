package net.dankito.data_access.network.communication.message

import net.dankito.utils.version.Version


data class RequestStartSynchronizationRequestBody(val deviceInfo: DeviceInfo, val synchronizationPort: Int = 0) {

    private constructor() : this(DeviceInfo("", "", Version(0, 0), 0)) // for Jackson

}

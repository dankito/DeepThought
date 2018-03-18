package net.dankito.data_access.network.communication.message


data class RequestStartSynchronizationRequestBody(val deviceInfo: DeviceInfo, val synchronizationPort: Int = 0, val fileSynchronizationPort: Int = 0) {

    private constructor() : this(DeviceInfo()) // for Jackson

}

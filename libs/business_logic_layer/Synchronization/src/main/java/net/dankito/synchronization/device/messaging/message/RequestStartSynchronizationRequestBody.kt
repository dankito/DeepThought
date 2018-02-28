package net.dankito.synchronization.device.messaging.message


data class RequestStartSynchronizationRequestBody(val deviceInfo: DeviceInfo, val synchronizationPort: Int = 0, val fileSynchronizationPort: Int = 0) {

    private constructor() : this(DeviceInfo()) // for Jackson

}

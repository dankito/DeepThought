package net.dankito.synchronization.device.messaging.message


data class RequestStartSynchronizationResponseBody(val result: RequestStartSynchronizationResult, val synchronizationPort: Int = 0,
                                                   val fileSynchronizationPort: Int = 0, val deviceInfo: DeviceInfo? = null) {

    private constructor() : this(RequestStartSynchronizationResult.DENIED)  // for Jackson

}

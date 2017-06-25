package net.dankito.data_access.network.communication.message


data class RequestStartSynchronizationResponseBody(val result: RequestStartSynchronizationResult, val synchronizationPort: Int = 0) {

    private constructor() : this(RequestStartSynchronizationResult.DENIED)  // for Jackson

}

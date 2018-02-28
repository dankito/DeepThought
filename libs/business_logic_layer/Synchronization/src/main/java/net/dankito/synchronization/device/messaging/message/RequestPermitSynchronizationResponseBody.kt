package net.dankito.synchronization.device.messaging.message


data class RequestPermitSynchronizationResponseBody(val result: RequestPermitSynchronizationResult, val nonce: String? = null) {

    private constructor() : this(RequestPermitSynchronizationResult.DENIED) // for Jackson

}

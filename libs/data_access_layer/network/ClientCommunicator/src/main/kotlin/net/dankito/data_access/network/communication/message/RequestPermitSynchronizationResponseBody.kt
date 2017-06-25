package net.dankito.data_access.network.communication.message


data class RequestPermitSynchronizationResponseBody(val result: RequestPermitSynchronizationResult, val nonce: String? = null) {

    private constructor() : this(RequestPermitSynchronizationResult.DENIED) // for Jackson

}

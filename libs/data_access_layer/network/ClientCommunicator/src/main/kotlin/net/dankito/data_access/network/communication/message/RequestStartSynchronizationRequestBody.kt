package net.dankito.data_access.network.communication.message


data class RequestStartSynchronizationRequestBody(val uniqueDeviceId: String, val synchronizationPort: Int = 0) {

    private constructor() : this("") // for Jackson

}

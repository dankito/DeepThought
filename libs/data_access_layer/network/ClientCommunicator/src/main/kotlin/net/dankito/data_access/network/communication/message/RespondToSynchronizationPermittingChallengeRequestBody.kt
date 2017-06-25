package net.dankito.data_access.network.communication.message


data class RespondToSynchronizationPermittingChallengeRequestBody(val nonce: String, val challengeResponse: String, val synchronizationPort: Int) {


    private constructor() : this("", "", 0) // for Jackson

}

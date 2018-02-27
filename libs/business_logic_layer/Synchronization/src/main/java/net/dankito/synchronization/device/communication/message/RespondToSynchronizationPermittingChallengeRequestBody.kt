package net.dankito.synchronization.device.communication.message

import net.dankito.synchronization.model.SyncInfo


data class RespondToSynchronizationPermittingChallengeRequestBody(val nonce: String, val challengeResponse: String, val syncInfo: SyncInfo, val synchronizationPort: Int,
                                                                  val fileSynchronizationPort: Int) {


    private constructor() : this("", "", SyncInfo(), 0, 0) // for Jackson

}

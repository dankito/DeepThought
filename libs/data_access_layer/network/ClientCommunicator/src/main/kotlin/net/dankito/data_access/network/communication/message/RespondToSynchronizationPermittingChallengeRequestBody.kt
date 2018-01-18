package net.dankito.data_access.network.communication.message

import net.dankito.service.synchronization.initialsync.model.SyncInfo


data class RespondToSynchronizationPermittingChallengeRequestBody(val nonce: String, val challengeResponse: String, val syncInfo: SyncInfo, val synchronizationPort: Int,
                                                                  val fileSynchronizationPort: Int) {


    private constructor() : this("", "", SyncInfo(), 0, 0) // for Jackson

}

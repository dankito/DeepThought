package net.dankito.data_access.network.communication.message

import net.dankito.service.synchronization.initialsync.model.SyncInfo


data class RespondToSynchronizationPermittingChallengeResponseBody(val result: RespondToSynchronizationPermittingChallengeResult,
                                                              val countRetriesLeft: Int = 0, val synchronizationPort: Int = 0,
                                                                   val syncInfo: SyncInfo? = null) {



    private constructor() : this(RespondToSynchronizationPermittingChallengeResult.DENIED) // for Jackson


    constructor(synchronizationPort: Int, syncInfo: SyncInfo) :
            this(RespondToSynchronizationPermittingChallengeResult.ALLOWED, synchronizationPort = synchronizationPort, syncInfo = syncInfo)

}

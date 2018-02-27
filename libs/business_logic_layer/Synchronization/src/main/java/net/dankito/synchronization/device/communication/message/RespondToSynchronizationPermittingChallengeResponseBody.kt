package net.dankito.synchronization.device.communication.message

import net.dankito.synchronization.model.SyncInfo


data class RespondToSynchronizationPermittingChallengeResponseBody(val result: RespondToSynchronizationPermittingChallengeResult,
                                                              val countRetriesLeft: Int = 0, val synchronizationPort: Int = 0, val fileSynchronizationPort: Int = 0,
                                                                   val syncInfo: SyncInfo? = null) {



    private constructor() : this(RespondToSynchronizationPermittingChallengeResult.DENIED) // for Jackson


    constructor(synchronizationPort: Int, fileSynchronizationPort: Int, syncInfo: SyncInfo) :
            this(RespondToSynchronizationPermittingChallengeResult.ALLOWED, synchronizationPort = synchronizationPort,
                    fileSynchronizationPort = fileSynchronizationPort, syncInfo = syncInfo)

}

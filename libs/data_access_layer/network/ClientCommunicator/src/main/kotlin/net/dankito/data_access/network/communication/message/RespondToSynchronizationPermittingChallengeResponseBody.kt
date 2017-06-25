package net.dankito.data_access.network.communication.message


data class RespondToSynchronizationPermittingChallengeResponseBody(val result: RespondToSynchronizationPermittingChallengeResult,
                                                              val countRetriesLeft: Int = 0, val synchronizationPort: Int = 0) {



    private constructor() : this(RespondToSynchronizationPermittingChallengeResult.DENIED) // for Jackson


    public constructor(synchronizationPort: Int) : this(RespondToSynchronizationPermittingChallengeResult.ALLOWED, synchronizationPort = synchronizationPort)

}

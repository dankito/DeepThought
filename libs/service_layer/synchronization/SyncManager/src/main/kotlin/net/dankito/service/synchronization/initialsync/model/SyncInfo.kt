package net.dankito.service.synchronization.initialsync.model


data class SyncInfo(val deepThought: DeepThoughtSyncInfo, val user: UserSyncInfo, val useCallerDatabaseIds: Boolean? = null, val useCallerUserName: Boolean? = null) {

    private constructor() : this(DeepThoughtSyncInfo(), UserSyncInfo()) // for Jackson
}
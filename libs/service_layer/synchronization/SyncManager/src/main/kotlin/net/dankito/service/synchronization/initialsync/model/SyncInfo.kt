package net.dankito.service.synchronization.initialsync.model


open class SyncInfo(val user: UserSyncInfo, val useCallerDatabaseIds: Boolean? = null, val useCallerUserName: Boolean? = null) {

    constructor() : this(UserSyncInfo(), false, false) // for Jackson
}
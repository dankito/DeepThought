package net.dankito.service.synchronization.initialsync.model


open class DeepThoughtSyncInfo(user: UserSyncInfo, useCallerDatabaseIds: Boolean?, useCallerUserName: Boolean?,
                               val deepThoughtId: String, val localDeviceId: String, val articleSummaryExtractorConfigs: List<ArticleSummaryExtractorConfig>)
    : SyncInfo(user, useCallerDatabaseIds, useCallerUserName) {

    internal constructor() : this(UserSyncInfo(), false, false, "", "", listOf()) // for Jackson

}
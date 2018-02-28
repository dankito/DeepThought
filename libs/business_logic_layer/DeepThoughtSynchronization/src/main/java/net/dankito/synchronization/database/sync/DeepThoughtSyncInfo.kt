package net.dankito.synchronization.database.sync

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.synchronization.model.SyncInfo
import net.dankito.synchronization.model.User
import net.dankito.synchronization.model.UserSyncInfo


open class DeepThoughtSyncInfo(localDeviceId: String, user: UserSyncInfo, useCallerDatabaseIds: Boolean?, useCallerUserName: Boolean?,
                               val articleSummaryExtractorConfigs: List<ArticleSummaryExtractorConfig>)
    : SyncInfo(localDeviceId, user, useCallerDatabaseIds, useCallerUserName) {

    internal constructor() : this("", UserSyncInfo(User("", "")), false, false, listOf()) // for Jackson

}
package net.dankito.synchronization.database.sync

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.synchronization.model.SyncInfo
import net.dankito.synchronization.model.User
import net.dankito.synchronization.model.UserSyncInfo


open class DeepThoughtSyncInfo(user: UserSyncInfo, useCallerDatabaseIds: Boolean?, useCallerUserName: Boolean?,
                               val localDeviceId: String, val articleSummaryExtractorConfigs: List<ArticleSummaryExtractorConfig>)
    : SyncInfo(user, useCallerDatabaseIds, useCallerUserName) {

    internal constructor() : this(UserSyncInfo(User("", "")), false, false, "", listOf()) // for Jackson

}
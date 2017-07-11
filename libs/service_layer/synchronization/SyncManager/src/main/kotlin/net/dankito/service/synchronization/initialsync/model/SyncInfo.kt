package net.dankito.service.synchronization.initialsync.model

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig


data class SyncInfo(val deepThought: DeepThoughtSyncInfo, val user: UserSyncInfo, val articleSummaryExtractorConfigs: List<ArticleSummaryExtractorConfig>,
                    val useCallerDatabaseIds: Boolean? = null, val useCallerUserName: Boolean? = null) {

    constructor() : this(DeepThoughtSyncInfo(), UserSyncInfo(), listOf()) // for Jackson
}
package net.dankito.service.data

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier


class ArticleSummaryExtractorConfigService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<ArticleSummaryExtractorConfig>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<ArticleSummaryExtractorConfig> {
        return ArticleSummaryExtractorConfig::class.java
    }

}
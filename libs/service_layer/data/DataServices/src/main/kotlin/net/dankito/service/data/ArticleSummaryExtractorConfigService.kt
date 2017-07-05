package net.dankito.service.data

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.ArticleSummaryExtractorConfigChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged


class ArticleSummaryExtractorConfigService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntityServiceBase<ArticleSummaryExtractorConfig>(dataManager, entityChangedNotifier) {

    override fun getEntityClass(): Class<ArticleSummaryExtractorConfig> {
        return ArticleSummaryExtractorConfig::class.java
    }

    override fun createEntityChangedMessage(entity: ArticleSummaryExtractorConfig, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return ArticleSummaryExtractorConfigChanged(entity, changeType)
    }

}
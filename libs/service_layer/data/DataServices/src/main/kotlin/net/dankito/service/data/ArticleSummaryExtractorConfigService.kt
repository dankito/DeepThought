package net.dankito.service.data

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.ArticleSummaryExtractorConfigChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.eventbus.IEventBus


class ArticleSummaryExtractorConfigService(dataManager: DataManager, eventBus: IEventBus) : EntityServiceBase<ArticleSummaryExtractorConfig>(dataManager, eventBus) {

    override fun getEntityClass(): Class<ArticleSummaryExtractorConfig> {
        return ArticleSummaryExtractorConfig::class.java
    }

    override fun createEntityChangedMessage(entity: ArticleSummaryExtractorConfig, changeType: EntityChangeType): EntityChanged<out BaseEntity> {
        return ArticleSummaryExtractorConfigChanged(entity, changeType)
    }

}
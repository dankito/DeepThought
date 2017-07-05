package net.dankito.service.data.event

import net.dankito.deepthought.model.*
import net.dankito.service.data.messages.*
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.messages.IEventBusMessage


class EntityChangedNotifier(private val eventBus: IEventBus) {

    fun notifyListenersOfEntityChange(entity: BaseEntity, changeType: EntityChangeType) {
        val entityClass = entity.javaClass

        createEntityChangedMessage(entityClass, entity, changeType)?.let { message -> // if entity has no extra EntityChanged message, return value is null
            eventBus.post(message) // has to be called synchronized so that LuceneSearchEngine can update its index before any other class accesses updated index
        }

        eventBus.postAsync(EntitiesOfTypeChanged(entityClass))
    }

    private fun createEntityChangedMessage(entityClass: Class<BaseEntity>, entity: BaseEntity, changeType: EntityChangeType): IEventBusMessage? {
        when(entityClass) {
            Entry::class.java -> return EntryChanged(entity as Entry, changeType)
            Tag::class.java -> return TagChanged(entity as Tag, changeType)
            Reference::class.java -> return ReferenceChanged(entity as Reference, changeType)
            ReadLaterArticle::class.java -> return ReadLaterArticleChanged(entity as ReadLaterArticle, changeType)
            ArticleSummaryExtractorConfig::class.java -> return ArticleSummaryExtractorConfigChanged(entity as ArticleSummaryExtractorConfig, changeType)
            else -> return null
        }
    }

}
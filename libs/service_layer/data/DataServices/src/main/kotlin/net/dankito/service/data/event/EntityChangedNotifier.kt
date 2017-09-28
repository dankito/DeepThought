package net.dankito.service.data.event

import net.dankito.deepthought.model.*
import net.dankito.service.data.messages.*
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.messages.IEventBusMessage


class EntityChangedNotifier(private val eventBus: IEventBus) {

    fun notifyListenersOfEntityChange(entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource) {
        val entityClass = entity.javaClass

        dispatchMessagesForChangedEntity(entityClass, entity, changeType, source)

        dispatchMessagesForDependentEntities(entityClass, entity, changeType, source)
    }

    private fun dispatchMessagesForChangedEntity(entityClass: Class<out BaseEntity>, entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource) {
        createEntityChangedMessage(entityClass, entity, changeType, source)?.let { message ->
            // if entity has no extra EntityChanged message, return value is null
            eventBus.post(message) // has to be called synchronized so that LuceneSearchEngine can update its index before any other class accesses updated index
        }

        eventBus.postAsync(EntitiesOfTypeChanged(entityClass, changeType, source))
    }

    private fun dispatchMessagesForDependentEntities(entityClass: Class<BaseEntity>, entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource) {
        if(entityClass == Tag::class.java) {
            dispatchMessagesForTagDependentEntities(entity as Tag, source)
        }
        else if(entityClass == Series::class.java) {
            dispatchMessagesForSeriesDependentEntities(entity as Series, source)
        }
        else if(entityClass == Reference::class.java) {
            dispatchMessagesForReferenceDependentEntities(entity as Reference, source)
        }
    }

    private fun dispatchMessagesForTagDependentEntities(tag: Tag, source: EntityChangeSource) {
        tag.entries.filterNotNull().forEach { entry ->
            notifyListenersOfEntityChange(entry, EntityChangeType.Updated, source)
        }
    }

    private fun dispatchMessagesForSeriesDependentEntities(series: Series, source: EntityChangeSource) {
        series.references.filterNotNull().forEach { reference ->
            notifyListenersOfEntityChange(reference, EntityChangeType.Updated, source)
        }
    }

    private fun dispatchMessagesForReferenceDependentEntities(reference: Reference, source: EntityChangeSource) {
        reference.entries.filterNotNull().forEach { entry ->
            notifyListenersOfEntityChange(entry, EntityChangeType.Updated, source)
        }
    }

    private fun createEntityChangedMessage(entityClass: Class<out BaseEntity>, entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource): IEventBusMessage? {
        when(entityClass) {
            Entry::class.java -> return EntryChanged(entity as Entry, changeType, source)
            Tag::class.java -> return TagChanged(entity as Tag, changeType, source)
            Reference::class.java -> return ReferenceChanged(entity as Reference, changeType, source)
            Series::class.java -> return SeriesChanged(entity as Series, changeType, source)
            ReadLaterArticle::class.java -> return ReadLaterArticleChanged(entity as ReadLaterArticle, changeType, source)
            ArticleSummaryExtractorConfig::class.java -> return ArticleSummaryExtractorConfigChanged(entity as ArticleSummaryExtractorConfig, changeType, source)
            else -> {
                if(Tag::class.java.isAssignableFrom(entityClass)) {
                    return TagChanged(entity as Tag, changeType, source)
                }
                else {
                    return null
                }
            }
        }
    }

}
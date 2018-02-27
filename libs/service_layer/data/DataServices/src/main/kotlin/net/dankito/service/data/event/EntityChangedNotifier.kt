package net.dankito.service.data.event

import net.dankito.deepthought.model.*
import net.dankito.service.data.messages.*
import net.dankito.service.eventbus.IEventBus
import net.dankito.synchronization.model.BaseEntity
import net.dankito.synchronization.model.LocalFileInfo
import net.dankito.util.AsyncProducerConsumerQueue


class EntityChangedNotifier(private val eventBus: IEventBus) {

    data class QueuedChange(val entity: BaseEntity, val changeType: EntityChangeType, val source: EntityChangeSource, val didChangesAffectingDependentEntities: Boolean)


    private val queue = AsyncProducerConsumerQueue<QueuedChange>(1) { item ->
        notifyListenersOfEntityChange(item.entity, item.changeType, item.source, item.didChangesAffectingDependentEntities)
    }


    fun notifyListenersOfEntityChangeAsync(entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource, didChangesAffectingDependentEntities: Boolean = false) {
        queue.add(QueuedChange(entity, changeType, source, didChangesAffectingDependentEntities))
    }

    fun notifyListenersOfEntityChange(entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource, didChangesAffectingDependentEntities: Boolean = false, isDependentChange: Boolean = false) {
        val entityClass = getEntityClass(entity)

        dispatchMessagesForChangedEntity(entityClass, entity, changeType, source, isDependentChange)

        if(didChangesAffectingDependentEntities) {
            dispatchMessagesForDependentEntities(entityClass, entity, changeType, source)
        }
    }

    private fun getEntityClass(entity: BaseEntity): Class<out BaseEntity> {
        if(entity is Tag) { // to also pass Tag's class for calculated tags
            return Tag::class.java
        }

        return entity.javaClass
    }

    private fun dispatchMessagesForChangedEntity(entityClass: Class<out BaseEntity>, entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource, isDependentChange: Boolean) {
        createEntityChangedMessage(entityClass, entity, changeType, source)?.let { message ->
            message.isDependentChange = isDependentChange

            // if entity has no extra EntityChanged message, return value is null
            eventBus.post(message) // has to be called synchronized so that LuceneSearchEngine can update its index before any other class accesses updated index
        }

        eventBus.postAsync(EntitiesOfTypeChanged(entityClass, changeType, source, isDependentChange))
    }

    private fun dispatchMessagesForDependentEntities(entityClass: Class<out BaseEntity>, entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource) {
        if(entityClass == Tag::class.java) {
            dispatchMessagesForTagDependentEntities(entity as Tag, source)
        }
        else if(entityClass == Series::class.java) {
            dispatchMessagesForSeriesDependentEntities(entity as Series, source)
        }
        else if(entityClass == Source::class.java) {
            dispatchMessagesForSourceDependentEntities(entity as Source, source)
        }
        else if(entityClass == FileLink::class.java) {
            dispatchMessagesForFileLinkDependentEntities(entity as FileLink, source)
        }
        else if(entityClass == LocalFileInfo::class.java) {
            dispatchMessagesForLocalFileInfoDependentEntities(entity as LocalFileInfo, source)
        }
    }

    private fun dispatchMessagesForTagDependentEntities(tag: Tag, source: EntityChangeSource) {
        tag.items.filterNotNull().forEach { item ->
            notifyListenersOfEntityChange(item, EntityChangeType.Updated, source)
        }
    }

    private fun dispatchMessagesForSeriesDependentEntities(series: Series, changeSource: EntityChangeSource) {
        series.sources.filterNotNull().forEach { source ->
            notifyListenersOfEntityChange(source, EntityChangeType.Updated, changeSource)
        }
    }

    private fun dispatchMessagesForSourceDependentEntities(source: Source, changeSource: EntityChangeSource) {
        source.items.filterNotNull().forEach { item ->
            notifyListenersOfEntityChange(item, EntityChangeType.Updated, changeSource)
        }
    }

    private fun dispatchMessagesForFileLinkDependentEntities(file: FileLink, changeSource: EntityChangeSource) {
        file.itemsAttachedTo.filterNotNull().forEach { item ->
            notifyListenersOfEntityChange(item, EntityChangeType.Updated, changeSource)
        }

        file.sourcesAttachedTo.filterNotNull().forEach { source ->
            notifyListenersOfEntityChange(source, EntityChangeType.Updated, changeSource)
        }
    }

    private fun dispatchMessagesForLocalFileInfoDependentEntities(localFileInfo: LocalFileInfo, source: EntityChangeSource) {
        notifyListenersOfEntityChange(localFileInfo.file, EntityChangeType.Updated, source)
    }

    private fun createEntityChangedMessage(entityClass: Class<out BaseEntity>, entity: BaseEntity, changeType: EntityChangeType, source: EntityChangeSource): EntityChanged<out BaseEntity>? {
        when(entityClass) {
            Item::class.java -> return ItemChanged(entity as Item, changeType, source)
            Tag::class.java -> return TagChanged(entity as Tag, changeType, source)
            Source::class.java -> return SourceChanged(entity as Source, changeType, source)
            Series::class.java -> return SeriesChanged(entity as Series, changeType, source)
            ReadLaterArticle::class.java -> return ReadLaterArticleChanged(entity as ReadLaterArticle, changeType, source)
            FileLink::class.java -> return FileChanged(entity as FileLink, changeType, source)
            LocalFileInfo::class.java -> return LocalFileInfoChanged(entity as LocalFileInfo, changeType, source)
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
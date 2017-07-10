package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.DocumentChange
import com.couchbase.lite.SavedRevision
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.deepthought.model.BaseEntity
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.utils.AsyncProducerConsumerQueue
import net.dankito.utils.ConsumerListener
import org.slf4j.LoggerFactory


class SynchronizedChangesHandler(private val entityManager: CouchbaseLiteEntityManagerBase, private val changeNotifier: EntityChangedNotifier) {

    companion object {
        private val MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY = 1500

        private val log = LoggerFactory.getLogger(SynchronizedChangesHandler::class.java)
    }


    private val dataMerger = SynchronizedDataMerger(entityManager)


    private var synchronizationChangesHandler = object : ConsumerListener<Database.ChangeEvent> {
        override fun consumeItem(item: Database.ChangeEvent) {
            handleSynchronizedChanges(item.changes)
        }
    }

    // wait some time before processing synchronized entities as they may have dependent entities which haven't been synchronized yet
    // yeah, i not, it would be better solving this event based instead of simply waiting, but currently i have no clue how this could be solved
    private var changeQueue = AsyncProducerConsumerQueue<Database.ChangeEvent>(1, AsyncProducerConsumerQueue.NO_LIMIT_ITEMS_TO_QUEUE,
            MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY, synchronizationChangesHandler)



    fun handleChange(event: Database.ChangeEvent) {
        if(event.isExternal) {
            changeQueue.add(event);
        }
    }


    private fun handleSynchronizedChanges(changes: List<DocumentChange>) {
        for (change in changes) {
            val entityType = getEntityTypeFromDocumentChange(change)
            val isBaseEntity = entityType != null && BaseEntity::class.java.isAssignableFrom(entityType)

            if(isBaseEntity) { // sometimes only some Couchbase internal data is synchronized without any user data -> skip these
                handleChange(change, entityType!!) // entityType has to be != null after check above
            }
            else if (isEntityDeleted(change)) {
                handleDeletedEntity(change)
            }
        }
    }


    private fun handleChange(change: DocumentChange, entityType: Class<out BaseEntity>) {
        if (change.isConflict) {
//            conflictHandler.handleConflict(change, entityType)
        }

        var synchronizedEntity = dataMerger.updateCachedSynchronizedEntity(change, entityType)
        if (synchronizedEntity == null) { // this entity is new to our side
            synchronizedEntity = entityManager.getEntityById(entityType, change.documentId)
        }

        if(synchronizedEntity != null) {
            changeNotifier.notifyListenersOfEntityChange(synchronizedEntity, EntityChangeType.Updated) // TODO: determine EntityChangeType
        }
    }


    private fun handleDeletedEntity(change: DocumentChange) {
        val id = change.documentId
        val document = entityManager.database.getDocument(id)
        if (document != null) {
            val lastUndeletedRevision = findLastUndeletedRevision(document)

            if (lastUndeletedRevision != null) {
                val entityType = getEntityTypeFromRevision(lastUndeletedRevision)
                if (entityType != null) {
                    val deletedEntity = entityManager.getEntityById(entityType, id)
                    if (deletedEntity != null) {
                        changeNotifier.notifyListenersOfEntityChange(deletedEntity, EntityChangeType.Deleted)
                    }
                }
            }
        }
    }

    private fun findLastUndeletedRevision(document: Document): SavedRevision? {
        try {
            val leafRevisions = document.getLeafRevisions()
            if (leafRevisions.size > 0) {
                var parentId: String? = leafRevisions.get(0).getParentId()

                while(parentId != null) {
                    val parentRevision = document.getRevision(parentId)

                    if(parentRevision == null) {
                        return null
                    }

                    if (parentRevision.isDeletion() == false) {
                        return parentRevision
                    }

                    parentId = parentRevision.getParentId()
                }
            }
        } catch (e: Exception) {
            log.error("Could not get Revision History for deleted Document with id " + document.getId(), e)
        }

        return null
    }

    private fun isEntityDeleted(change: DocumentChange): Boolean {
        return change.addedRevision.isDeleted
    }

    private fun getEntityTypeFromRevision(revision: SavedRevision): Class<out BaseEntity>? {
        val entityTypeString = revision.getProperty(Dao.TYPE_COLUMN_NAME) as String

        return getEntityTypeFromEntityTypeString(entityTypeString)
    }

    private fun getEntityTypeFromDocumentChange(change: DocumentChange): Class<out BaseEntity>? {
        val entityTypeString = change.addedRevision.getPropertyForKey(Dao.TYPE_COLUMN_NAME) as? String

        return getEntityTypeFromEntityTypeString(entityTypeString)
    }

    private fun getEntityTypeFromEntityTypeString(entityTypeString: String?): Class<out BaseEntity>? {
        if (entityTypeString != null) { // sometimes there are documents without type or any other column/property except Couchbase's system properties (like _id)
            try {
                return Class.forName(entityTypeString) as? Class<out BaseEntity>
            } catch (e: Exception) {
                log.error("Could not get class for entity type " + entityTypeString)
            }
        }

        return null
    }

}
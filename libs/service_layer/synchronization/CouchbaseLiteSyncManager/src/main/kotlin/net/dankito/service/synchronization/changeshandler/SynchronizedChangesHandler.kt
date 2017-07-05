package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.Database
import com.couchbase.lite.DocumentChange
import com.couchbase.lite.SavedRevision
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.BaseEntity
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.utils.AsyncProducerConsumerQueue
import net.dankito.utils.ConsumerListener
import org.slf4j.LoggerFactory


class SynchronizedChangesHandler(private val entityManager: IEntityManager, private val changeNotifier: EntityChangedNotifier) {

    companion object {
        private val MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY = 1500

        private val log = LoggerFactory.getLogger(SynchronizedChangesHandler::class.java)
    }


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

            if(entityType != null) {
                handleChange(change, entityType)
            }
            else if (isEntityDeleted(change)) {
//                handleDeletedEntity(change)
            }
        }
    }


    private fun handleChange(change: DocumentChange, entityType: Class<out BaseEntity>) {
        if (change.isConflict) {
//            conflictHandler.handleConflict(change, entityType)
        }

//        var synchronizedEntity = dataMerger.updateCachedSynchronizedEntity(change, entityType)
        var synchronizedEntity: BaseEntity? = null
        if (synchronizedEntity == null) { // this entity is new to our side
            synchronizedEntity = entityManager.getEntityById(entityType, change.documentId)
        }

        if(synchronizedEntity != null) {
            changeNotifier.notifyListenersOfEntityChange(synchronizedEntity, EntityChangeType.Updated) // TODO: determine EntityChangeType
        }
    }


    private fun isEntityDeleted(change: DocumentChange): Boolean {
        return change.addedRevision.isDeleted
    }

    private fun getEntityTypeFromRevision(revision: SavedRevision): Class<out BaseEntity>? {
        val entityTypeString = revision.getProperty(Dao.TYPE_COLUMN_NAME) as String

        return getEntityTypeFromEntityTypeString(entityTypeString)
    }

    private fun getEntityTypeFromDocumentChange(change: DocumentChange): Class<out BaseEntity>? {
        val entityTypeString = change.addedRevision.getPropertyForKey(Dao.TYPE_COLUMN_NAME) as String

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
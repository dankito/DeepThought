package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.DocumentChange
import com.couchbase.lite.SavedRevision
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.utils.AsyncProducerConsumerQueue
import org.slf4j.LoggerFactory


class SynchronizedChangesHandler(private val entityManager: CouchbaseLiteEntityManagerBase, private val changeNotifier: EntityChangedNotifier) : ISynchronizedChangesHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SynchronizedChangesHandler::class.java)
    }


    private val dataMerger = SynchronizedDataMerger(entityManager)


    // wait some time before processing synchronized entities as they may have dependent entities which haven't been synchronized yet
    // yeah, i not, it would be better solving this event based instead of simply waiting, but currently i have no clue how this could be solved
    private var changeQueue = AsyncProducerConsumerQueue<Database.ChangeEvent>(1) { changeEvent ->
        handleSynchronizedChanges(changeEvent.changes)
    }




    override fun handleChange(event: Database.ChangeEvent) {
        if(event.isExternal) {
            changeQueue.add(event)
        }
    }


    private fun handleSynchronizedChanges(changes: List<DocumentChange>) {
        for(change in changes) {
            try {
                handleChange(change)
            } catch(e: Exception) { log.error("Could not handle change for document ${change.documentId}: $change", e) }
        }
    }

    private fun handleChange(change: DocumentChange) {
        val entityType = getEntityTypeFromDocumentChange(change)
        val isBaseEntity = entityType != null && BaseEntity::class.java.isAssignableFrom(entityType)

        log.info("Handling synchronized change of type $entityType with id ${change.documentId} for revision ${change.addedRevision.revID}, isBaseEntity = $isBaseEntity")

        if(isBaseEntity) { // sometimes only some Couchbase internal data is synchronized without any user data -> skip these
            if(isEntityDeleted(change)) {
                handleDeletedEntity(change, entityType!!)
            }
            else {
                handleChange(change, entityType!!) // entityType has to be != null after check above
            }
        }
        else if(isEntityDeleted(change)) {
            handleDeletedEntity(change)
        }
    }


    private fun handleChange(change: DocumentChange, entityType: Class<out BaseEntity>) {
        if (change.isConflict) {
//            conflictHandler.handleConflict(change, entityType)
        }

        var synchronizedEntity = dataMerger.updateCachedSynchronizedEntity(change, entityType)
        var entityChangeType = EntityChangeType.Updated
        if(synchronizedEntity == null) { // this entity is either new to our side or hasn't been in cache
            // TODO: if it hasn't been created, shouldn't we then avoid loading it from db as it means as not cached means: Not loaded and therefore not displayed on our side -> we don't have to update any UI elements
            synchronizedEntity = entityManager.getEntityById(entityType, change.documentId)

            if(hasEntityBeenCreated(change, synchronizedEntity)) {
                entityChangeType = EntityChangeType.Created
            }
        }


        if(synchronizedEntity != null) {
            if(change.isConflict) {
                log.warn("$synchronizedEntity has a conflict")
            }
            notifyOfSynchronizedChange(synchronizedEntity, entityChangeType)
        }
    }

    private fun hasEntityBeenCreated(change: DocumentChange, entity: BaseEntity?): Boolean {
        try {
            val document = entityManager.database.getDocument(change.documentId)

            // ah, figured it out, current (06.03.2018) implementation of Dao first creates an empty document in DB (= revision 1) and then adds and stores entity's properties (= revision 2)
            if(entity?.version == 2L) { // TODO: get version from revision, so that we don't need entity instance
                return true
            }

            val leafRevisions = document.leafRevisions
            if(leafRevisions.size == 0) {
                return true
            }
            else if(leafRevisions[0].parentId == null) {
                return true
            }
            else {
                val parentRevision = document.getRevision(leafRevisions[0].parentId)
                return parentRevision == null
            }
        } catch (ignored: Exception) { }

        return (entity != null && entity.version == 1L)
    }


    private fun handleDeletedEntity(change: DocumentChange) {
        val id = change.documentId
        val document = entityManager.database.getDocument(id)
        if(document != null) {
            val lastUndeletedRevision = findLastUndeletedRevision(document)

            if(lastUndeletedRevision != null) {
                val entityType = getEntityTypeFromRevision(lastUndeletedRevision)
                if(entityType != null) {
                    handleDeletedEntity(id, entityType, document)
                }
            }
        }
    }

    private fun handleDeletedEntity(change: DocumentChange, entityType: Class<out BaseEntity>) {
        val id = change.documentId
        entityManager.database.getDocument(id)?.let {
            handleDeletedEntity(id, entityType, it)
        }
    }

    private fun handleDeletedEntity(id: String, entityType: Class<out BaseEntity>, document: Document) {
        getDeletedEntity(id, entityType, document)?.let { deletedEntity ->
            notifyOfSynchronizedChange(deletedEntity, EntityChangeType.Deleted)
        }
    }

    private fun getDeletedEntity(id: String, entityType: Class<out BaseEntity>, document: Document): BaseEntity? {
        entityManager.getEntityById(entityType, id)?.let {
            return it
        }

        val dao = entityManager.getDaoForClass(entityType)
        dao?.createObjectFromDocument(document, id, entityType)?.let {
            return it as? BaseEntity
        }

        return null
    }

    private fun findLastUndeletedRevision(document: Document): SavedRevision? {
        try {
            val leafRevisions = document.leafRevisions
            if (leafRevisions.size > 0) {
                var parentId: String? = leafRevisions[0].parentId

                while(parentId != null) {
                    val parentRevision = document.getRevision(parentId)

                    if(parentRevision == null) {
                        return null
                    }

                    if (parentRevision.isDeletion == false) {
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


    private fun notifyOfSynchronizedChange(synchronizedEntity: BaseEntity, changeType: EntityChangeType) {
        try { log.info("Calling notifyListenersOfEntityChange() for $synchronizedEntity of type $changeType") } // toString() may throws an exception for deleted entities
        catch(ignored: Exception) { log.info("Calling notifyListenersOfEntityChange() for ${synchronizedEntity.id} of type $changeType") }

        // TODO: check for which entities it should be set to true. E.g. Series would notify a lot of Sources without benefit, the same with tags
        // e.g. an item has been synchronized without its source -> source couldn't be retrieved from database. Now source gets synchronized -> item gets updated and its source therefore set
        val didChangesAffectingDependentEntities = !( synchronizedEntity is Series || synchronizedEntity is Tag)
        changeNotifier.notifyListenersOfEntityChangeAsync(synchronizedEntity, changeType, EntityChangeSource.Synchronization, didChangesAffectingDependentEntities)
    }

}
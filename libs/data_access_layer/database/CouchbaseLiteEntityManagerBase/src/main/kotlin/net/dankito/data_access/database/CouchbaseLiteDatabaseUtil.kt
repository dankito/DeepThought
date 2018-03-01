package net.dankito.data_access.database

import com.couchbase.lite.ChangesOptions
import com.couchbase.lite.Document
import com.couchbase.lite.SavedRevision
import net.dankito.deepthought.model.Item
import net.dankito.jpa.couchbaselite.CouchbaseLiteEntityManagerBase
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.jpa.entitymanager.ChangedEntity
import net.dankito.utils.database.IDatabaseUtil
import net.dankito.synchronization.model.BaseEntity
import net.dankito.util.AsyncProducerConsumerQueue
import org.slf4j.LoggerFactory
import java.lang.Exception


class CouchbaseLiteDatabaseUtil(private val entityManager: CouchbaseLiteEntityManagerBase) : IDatabaseUtil {

    companion object {
        private val log = LoggerFactory.getLogger(CouchbaseLiteDatabaseUtil::class.java)
    }


    /**
     * Passes all entities that have been updated after (Couchbase database) sequenceNumber on to queue.
     * Using a AsyncProducerConsumerQueue so that retrieved entities can be consumed immediately and not loading all of them into memory first
     * which could result in an OutOfMemory exception when there are too many changes for little Android's memory.
     */
    override fun <T> getAllEntitiesUpdatedAfter(sequenceNumber: Long, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>): Long {
        val database = entityManager.database
        val currentSequenceNumber = database.lastSequenceNumber

        val options = ChangesOptions(Int.MAX_VALUE, true, true, true)
        val changes = database.changesSince(sequenceNumber, options, null, null)
        log.info("Retrieved ${changes.allDocIds.size} changes since $sequenceNumber")

        entityManager.getDaoForClass(Item::class.java)?.let { anyDao -> // get any Dao, no matter which one
            changes.allDocIds.forEach { docId ->
                getUpdatedEntityFromDocument(docId, anyDao, queue)
            }
        }

        return currentSequenceNumber
    }

    private fun <T> getUpdatedEntityFromDocument(documentId: String, anyDao: Dao, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        try {
            entityManager.database.getDocument(documentId)?.let {
                getUpdatedEntityFromDocument(it, anyDao, queue)
            }
        } catch(e: Exception) {
            log.error("Could not get updated entity for document id $documentId", e)
        }
    }

    private fun <T> getUpdatedEntityFromDocument(document: Document, anyDao: Dao, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        if(document.isDeleted || document.getProperty(Dao.DELETED_SYSTEM_COLUMN_NAME) == true) {
            getDeletedEntityFromDocument(document, queue)
        }
        else {
            anyDao.getEntityClassFromDocument(document)?.let { entityClass ->
                getObjectForDocument(entityClass as Class<T>, document, queue)
            }
        }
    }

    private fun <T> getObjectForDocument(entityClass: Class<T>, document: Document, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        (entityManager.objectCache.get(entityClass, document.id) as? T)?.let { // first check if an object for that id is already cached
            queue.add(ChangedEntity<T>(entityClass, it, (it as BaseEntity).id))
            return
        }

        entityManager.getDaoForClass(entityClass)?.let { dao ->
            (dao.createObjectFromDocument(document, document.id, entityClass) as? T)?.let { entity ->
                queue.add(ChangedEntity<T>(entityClass, entity, (entity as BaseEntity).id))
            }
        }
    }

    private fun <T> getDeletedEntityFromDocument(document: Document, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>) {
        findLastUndeletedRevision(document)?.let { lastUndeletedRevision ->
            getEntityTypeFromRevision(lastUndeletedRevision)?.let { entityType ->
                try {
                    queue.add(ChangedEntity(entityType as Class<T>, null, document.id, true))
                } catch(e: Exception) { log.error("Could not get deleted entity for entity $entityType with id ${document.id}", e) }
            }
        }
    }


    private fun loadDeletedEntity(documentId: String): BaseEntity? {
        val document = entityManager.database.getDocument(documentId)
        if (document != null) {
            val lastUndeletedRevision = findLastUndeletedRevision(document)

            if (lastUndeletedRevision != null) {
                val entityType = getEntityTypeFromRevision(lastUndeletedRevision)
                if (entityType != null) {
                    return getDeletedEntity(documentId, entityType, lastUndeletedRevision.document)
                }
            }
        }

        return null
    }

    private fun getDeletedEntity(id: String, entityType: Class<out BaseEntity>, document: Document): BaseEntity? {
//        getEntityById(entityType, id)?.let {
//            return it
//        }

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

    private fun getEntityTypeFromRevision(revision: SavedRevision): Class<out BaseEntity>? {
        val entityTypeString = revision.getProperty(Dao.TYPE_COLUMN_NAME) as String

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
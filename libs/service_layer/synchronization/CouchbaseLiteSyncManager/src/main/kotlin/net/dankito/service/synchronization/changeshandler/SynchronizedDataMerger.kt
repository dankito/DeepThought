package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.Database
import com.couchbase.lite.DocumentChange
import com.couchbase.lite.SavedRevision
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.config.TableConfig
import net.dankito.jpa.apt.config.ColumnConfig
import net.dankito.jpa.apt.config.EntityConfig
import net.dankito.jpa.couchbaselite.Dao
import net.dankito.jpa.relationship.collections.EntitiesCollection
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*


class SynchronizedDataMerger(private val entityManager: CouchbaseLiteEntityManagerBase) {

    companion object {
        private val log = LoggerFactory.getLogger(SynchronizedDataMerger::class.java)
    }


    private val database: Database = entityManager.database


    fun updateCachedSynchronizedEntity(change: DocumentChange, entityType: Class<out BaseEntity>): BaseEntity? {
        var cachedEntity: BaseEntity? = null

        try {
            cachedEntity = entityManager.objectCache.get(entityType, change.documentId) as? BaseEntity
            cachedEntity?.let { entity -> // cachedEntity == null: Entity not retrieved / cached yet -> will be read from DB on next access anyway, therefore no need to  update it
                log.info("Updating cached synchronized Entity of Revision " + change.revisionId + ": " + cachedEntity)

                val storedDocument = database.getExistingDocument(change.documentId)
                entityManager.getDaoForClass(entityType)?.let { dao ->
                    val currentRevision = storedDocument.currentRevision

                    updateCachedEntity(entity, dao, currentRevision)
                }
            }
        } catch (e: Exception) {
            log.error("Could not handle Change", e)
        }

        if (isEntityDeleted(cachedEntity, change)) {
            if (cachedEntity == null) {
                cachedEntity = entityManager.getEntityById(entityType, change.documentId)
            }
        }

        return cachedEntity
    }


    private fun isEntityDeleted(cachedEntity: BaseEntity?, change: DocumentChange): Boolean {
        if (cachedEntity != null) {
            return cachedEntity.deleted
        }
        else {
            try {
                return change.addedRevision.getPropertyForKey(TableConfig.BaseEntityDeletedColumnName) as Boolean
            } catch (ignored: Exception) {
            }

        }

        return false
    }


    @Throws(SQLException::class)
    private fun updateCachedEntity(cachedEntity: BaseEntity, dao: Dao, currentRevision: SavedRevision) {
        val entityConfig = dao.entityConfig
        val detectedChanges = getChanges(cachedEntity, dao, entityConfig, currentRevision)

        if (detectedChanges.isNotEmpty()) {
            for (propertyName in detectedChanges.keys) {
                try {
                    updateProperty(cachedEntity, propertyName, dao, entityConfig, currentRevision, detectedChanges)
                } catch (e: Exception) {
                    log.error("Could not update Property $propertyName on synchronized Object $cachedEntity", e)
                }

            }
        }
    }

    @Throws(SQLException::class)
    private fun updateProperty(cachedEntity: BaseEntity, propertyName: String, dao: Dao, entityConfig: EntityConfig, currentRevision: SavedRevision, detectedChanges: Map<String, Any>) {
        entityConfig.columns.filter { it.columnName == propertyName }.firstOrNull()?.let { property ->
            val previousValue = dao.extractValueFromObject(cachedEntity, property)

            if (property.isToManyColumn() == false) {
                val updatedValue = dao.deserializePersistedValue(cachedEntity, property, currentRevision.getProperty(propertyName))
                dao.setValueOnObject(cachedEntity, property, updatedValue)
            } else {
                updateCollectionProperty(cachedEntity, property, propertyName, currentRevision, detectedChanges, previousValue)
            }
        }
    }

    @Throws(SQLException::class)
    private fun updateCollectionProperty(cachedEntity: BaseEntity, property: ColumnConfig, propertyName: String, currentRevision: SavedRevision, detectedChanges: Map<String, Any>,
                                           previousValue: Any) {
        val previousTargetEntityIdsString = detectedChanges[propertyName] as String
        var currentTargetEntityIdsString = currentRevision.getProperty(propertyName) as String
        if (currentRevision.properties.containsKey(propertyName) == false) { // currentRevision has no information about this property
            currentTargetEntityIdsString = "[]" // TODO: what to do here? Assuming "[]" is for sure false. Removing all items?
        }

        property.targetEntity?.entityClass?.let { targetEntityClass ->
            entityManager.getDaoForClass(targetEntityClass)?.let { targetDao ->
                val currentTargetEntityIds = targetDao.parseJoinedEntityIdsFromJsonString(currentTargetEntityIdsString)

                log.info("Collection Property " + property + " of Revision " + currentRevision.id + " has now Ids of " + currentTargetEntityIdsString + ". Previous ones: " + previousTargetEntityIdsString)

                if (previousValue is EntitiesCollection) { // TODO: what to do if it's not an EntitiesCollection yet?
                    previousValue.refresh(currentTargetEntityIds)
                }
                else {
                    log.warn("Not an EntitiesCollection: " + previousValue)
                }
            }
        }
    }

    private fun getChanges(cachedEntity: BaseEntity, dao: Dao, entityConfig: EntityConfig, currentRevision: SavedRevision): Map<String, Any> {
        val detectedChanges = HashMap<String, Any>()

        for(columnConfig in entityConfig.getColumnsIncludingInheritedOnes()) {
            if (columnConfig.isId || columnConfig.isVersion /*|| columnConfig is DiscriminatorColumnConfig*/ ||
                    TableConfig.BaseEntityModifiedOnColumnName == columnConfig.columnName) {
                continue
            }

            try {
                val cachedEntityValue = dao.getPersistablePropertyValue(cachedEntity, columnConfig)

                if (hasPropertyValueChanged(cachedEntity, columnConfig, cachedEntityValue, dao, currentRevision)) {
                    detectedChanges.put(columnConfig.columnName, cachedEntityValue)
                }
            } catch (e: Exception) {
                log.error("Could not check Property $columnConfig for changes", e)
            }

        }

        return detectedChanges
    }

    @Throws(SQLException::class)
    protected fun hasPropertyValueChanged(cachedEntity: BaseEntity, columnConfig: ColumnConfig, cachedEntityValue: Any?, dao: Dao, currentRevision: SavedRevision): Boolean {
        var currentRevisionValue: Any? = currentRevision.properties[columnConfig.columnName]

        if (columnConfig.isLob) {
            currentRevisionValue = dao.getLobFromAttachment(columnConfig, currentRevision.document)
            if (currentRevisionValue !== cachedEntityValue) {
                dao.setValueOnObject(cachedEntity, columnConfig, currentRevisionValue) // TODO: this produces a side effect, but i would have to change structure too hard to implement this little feature

                if (cachedEntityValue != null && cachedEntityValue is ByteArray && dao.shouldCompactDatabase(cachedEntityValue.size.toLong())) {
                    dao.compactDatabase()
                }
            }
        }
        else if(columnConfig.isToManyColumn() == false) {
            if (cachedEntityValue == null && currentRevisionValue != null || cachedEntityValue != null && currentRevisionValue == null ||
                    cachedEntityValue != null && cachedEntityValue == currentRevisionValue == false) {
                return true
            }
        }
        else {
            if (hasCollectionPropertyChanged(dao, currentRevisionValue, cachedEntityValue)) {
                return true
            }
        }

        return false
    }

    @Throws(SQLException::class)
    protected fun hasCollectionPropertyChanged(dao: Dao, currentRevisionValue: Any?, cachedEntityValue: Any?): Boolean {
        if (currentRevisionValue == null || cachedEntityValue == null) {
            return currentRevisionValue !== cachedEntityValue // if only one of them is null, than there's a change
        }

        val currentRevisionTargetEntityIds = dao.parseJoinedEntityIdsFromJsonString(currentRevisionValue as String?)
        val cachedEntityTargetEntityIds = dao.parseJoinedEntityIdsFromJsonString(cachedEntityValue as String?)

        if (currentRevisionTargetEntityIds.size != cachedEntityTargetEntityIds.size) {
            return true
        }

        for (targetEntityId in currentRevisionTargetEntityIds) {
            if (cachedEntityTargetEntityIds.contains(targetEntityId) == false) {
                return true
            }
        }

        return false // cachedEntityTargetEntityIds contains all targetEntityIds of currentRevisionTargetEntityIds
    }

}

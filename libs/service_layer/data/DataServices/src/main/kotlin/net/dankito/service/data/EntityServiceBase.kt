package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeType
import kotlin.concurrent.thread


abstract class EntityServiceBase<T : BaseEntity>(val entityClass: Class<T>, val dataManager: DataManager, val entityChangedNotifier: EntityChangedNotifier) {

    val entityManager = dataManager.entityManager


    fun getAllAsync(callback: (List<T>) -> Unit) {
        thread { // TODO: use IThreadPool
            callback(getAll())
        }
    }

    fun getAll() : List<T> {
        return entityManager.getAllEntitiesOfType(entityClass)
    }


    fun persist(entity: T) {
        synchronized(this) {
            onPrePersist(entity)

            // we first have to persist an entity so that it gets an id and than can add it to DeepThought (otherwise it would be added to DeepThought with id null)
            entityManager.persistEntity(entity as Any)
        }

        callEntitiesUpdatedListeners(entity, EntityChangeType.Created)
    }

    protected open fun onPrePersist(entity: T) {
        // may be overwritten in sub class
    }


    fun retrieve(id: String): T? {
        return entityManager.getEntityById(entityClass, id)
    }

    fun update(entity: T) {
        entityManager.updateEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Updated)
    }

    fun delete(entity: T) {
        callEntitiesUpdatedListeners(entity, EntityChangeType.PreDelete) // as after deleting entity from db entity's id is null -> for services still needing entity's id call PreDelete

        entityManager.deleteEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Deleted)
    }


    private fun callEntitiesUpdatedListeners(entity: T, changeType: EntityChangeType) {
        entityChangedNotifier.notifyListenersOfEntityChange(entity, changeType)
    }

}

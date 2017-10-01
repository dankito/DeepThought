package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import kotlin.concurrent.thread


abstract class EntityServiceBase<T : BaseEntity>(val entityClass: Class<T>, val dataManager: DataManager, val entityChangedNotifier: EntityChangedNotifier) {

    val entityManager = dataManager.entityManager


    fun getAllAsync(callback: (List<T>) -> Unit) {
        thread { // TODO: use IThreadPool
            callback(getAll())
        }
    }

    open fun getAll() : List<T> {
        return entityManager.getAllEntitiesOfType(entityClass)
    }


    open fun persist(entity: T) {
        synchronized(this) {
            onPrePersist(entity)

            // we first have to persist an entity so that it gets an id and than can add it to DeepThought (otherwise it would be added to DeepThought with id null)
            entityManager.persistEntity(entity)
        }

        callEntitiesUpdatedListeners(entity, EntityChangeType.Created)
    }

    protected open fun onPrePersist(entity: T) {
        // may be overwritten in sub class
    }


    open fun retrieve(id: String): T? {
        return entityManager.getEntityById(entityClass, id)
    }

    open fun update(entity: T, didChangesAffectingDependentEntities: Boolean = false) {
        entityManager.updateEntity(entity)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Updated, didChangesAffectingDependentEntities)
    }

    open fun delete(entity: T) {
        callEntitiesUpdatedListeners(entity, EntityChangeType.PreDelete) // as after deleting entity from db entity's id is null -> for services still needing entity's id call PreDelete

        entityManager.deleteEntity(entity)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Deleted)
    }


    private fun callEntitiesUpdatedListeners(entity: T, changeType: EntityChangeType, didChangesAffectingDependentEntities: Boolean = false) {
        entityChangedNotifier.notifyListenersOfEntityChangeAsync(entity, changeType, EntityChangeSource.Local, didChangesAffectingDependentEntities)
    }

}

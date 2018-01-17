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

            entityManager.persistEntity(entity)

            onPostPersist(entity)
        }

        callEntitiesUpdatedListenersForCreatedEntity(entity)
    }

    protected open fun onPrePersist(entity: T) {
        // may be overwritten in sub class
    }

    protected open fun onPostPersist(entity: T) {
        // may be overwritten in sub class
    }

    protected open fun callEntitiesUpdatedListenersForCreatedEntity(entity: T) {
        callEntitiesUpdatedListeners(entity, EntityChangeType.Created)
    }


    open fun retrieve(id: String): T? {
        return entityManager.getEntityById(entityClass, id)
    }

    open fun update(entity: T, didChangesAffectingDependentEntities: Boolean = false) {
        entityManager.updateEntity(entity)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Updated, didChangesAffectingDependentEntities)
    }

    open fun delete(entity: T) {
        callEntitiesUpdatedListenersSynchronously(entity, EntityChangeType.PreDelete) // as after deleting entity from db entity's id is null -> for services still needing entity's id call  PreDelete synchronously

        entityManager.deleteEntity(entity)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Deleted)
    }


    protected fun callEntitiesUpdatedListeners(entity: T, changeType: EntityChangeType, didChangesAffectingDependentEntities: Boolean = false) {
        entityChangedNotifier.notifyListenersOfEntityChangeAsync(entity, changeType, EntityChangeSource.Local, didChangesAffectingDependentEntities)
    }

    protected fun callEntitiesUpdatedListenersSynchronously(entity: T, changeType: EntityChangeType, didChangesAffectingDependentEntities: Boolean = false) {
        entityChangedNotifier.notifyListenersOfEntityChange(entity, changeType, EntityChangeSource.Local, didChangesAffectingDependentEntities)
    }

}

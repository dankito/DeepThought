package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.eventbus.IEventBus
import kotlin.concurrent.thread


abstract class EntityServiceBase<T : BaseEntity>(val dataManager: DataManager, val eventBus: IEventBus) {

    val entityManager = dataManager.entityManager


    fun getAllAsync(callback: (List<T>) -> Unit) {
        thread { // TODO: use IThreadPool
            callback(getAll())
        }
    }

    fun getAll() : List<T> {
        return entityManager.getAllEntitiesOfType(getEntityClass())
    }

    abstract fun getEntityClass(): Class<T>


    fun persist(entity: T) {
        onPrePersist(entity)

        // we first have to persist an entity so that it gets an id and than can add it to DeepThought (otherwise it would be added to DeepThought with id null)
        entityManager.persistEntity(entity as Any)

        dataManager.currentDeepThought?.let {
            if(addEntityToDeepThought(it, entity)) {
                entityManager.updateEntity(it)
            }
        }

        callEntitiesUpdatedListeners(entity, EntityChangeType.Created)
    }

    protected open fun onPrePersist(entity: T) {
        // may be overwritten in sub class
    }

    protected abstract fun addEntityToDeepThought(deepThought: DeepThought, entity: T): Boolean


    fun retrieve(id: String): T? {
        return entityManager.getEntityById(getEntityClass(), id)
    }

    fun update(entity: T) {
        entityManager.updateEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Updated)
    }

    fun delete(entity: T) {
        dataManager.currentDeepThought?.let {
            if(removeEntityFromDeepThought(it, entity)) {
                entityManager.updateEntity(it)
            }
        }

        entityManager.deleteEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Deleted)
    }

    abstract fun removeEntityFromDeepThought(deepThought: DeepThought, entity: T): Boolean


    private fun callEntitiesUpdatedListeners(entity: T, changeType: EntityChangeType) {
        eventBus.post(createEntityChangedMessage(entity, changeType)) // has to be called synchronized so that LuceneSearchEngine can update its index before any other class  accesses updated index

        eventBus.postAsync(EntitiesOfTypeChanged(getEntityClass()))
    }

    abstract fun createEntityChangedMessage(entity: T, changeType: EntityChangeType): EntityChanged<out BaseEntity>

}

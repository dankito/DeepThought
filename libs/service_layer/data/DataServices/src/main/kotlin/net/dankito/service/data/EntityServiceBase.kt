package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.EntityChanged
import net.dankito.service.eventbus.IEventBus


abstract class EntityServiceBase<T : BaseEntity>(dataManager: DataManager, val eventBus: IEventBus) {

    val entityManager = dataManager.entityManager


    fun getAllAsync(callback: (List<T>) -> Unit) {
        callback(getAll())
    }

    fun getAll() : List<T> {
        return entityManager.getAllEntitiesOfType(getEntityClass())
    }

    abstract fun getEntityClass(): Class<T>


    fun persist(entity: T) {
        entityManager.persistEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Created)
    }

    fun update(entity: T) {
        entityManager.updateEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Updated)
    }

    fun delete(entity: T) {
        entityManager.deleteEntity(entity as Any)

        callEntitiesUpdatedListeners(entity, EntityChangeType.Deleted)
    }


    private fun callEntitiesUpdatedListeners(entity: T, changeType: EntityChangeType) {
        eventBus.post(createEntityChangedMessage(entity, changeType)) // has to be called synchronized so that LuceneSearchEngine can update its index before any other class  accesses updated index

        eventBus.postAsync(EntitiesOfTypeChanged(getEntityClass()))
    }

    abstract fun createEntityChangedMessage(entity: T, changeType: EntityChangeType): EntityChanged<out BaseEntity>

}

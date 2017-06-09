package net.dankito.service.data

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.service.data.DataManager


abstract class EntityServiceBase<T : BaseEntity>(dataManager: DataManager) {

    protected val entityManager = dataManager.entityManager

    protected val entitiesUpdatedListeners = mutableSetOf<() -> Unit>()


    fun getAllAsync(callback: (List<T>) -> Unit) {
        callback(getEntries())
    }

    fun getEntries() : List<T> {
        return entityManager.getAllEntitiesOfType(getEntityClass())
    }

    abstract fun getEntityClass(): Class<T>


    fun persist(entity: T) {
        entityManager.persistEntity(entity as Any)

        callEntitiesUpdatedListeners()
    }

    fun update(entity: T) {
        entityManager.updateEntity(entity as Any)

        callEntitiesUpdatedListeners()
    }

    fun delete(entity: T) {
        entityManager.deleteEntity(entity as Any)

        callEntitiesUpdatedListeners()
    }


    fun addEntitiesUpdatedListener(listener: () -> Unit) {
        entitiesUpdatedListeners.add(listener)
    }

    fun removeEntitiesUpdatedListener(listener: () -> Unit) {
        entitiesUpdatedListeners.remove(listener)
    }

    private fun callEntitiesUpdatedListeners() {
        entitiesUpdatedListeners.forEach { it() }
    }

}

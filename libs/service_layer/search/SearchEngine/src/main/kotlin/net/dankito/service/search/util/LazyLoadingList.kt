package net.dankito.service.search.util

import net.dankito.data_access.database.IEntityManager
import net.dankito.synchronization.model.BaseEntity
import org.slf4j.LoggerFactory
import java.util.*


open class LazyLoadingList<T : BaseEntity>(protected var entityManager: IEntityManager, protected var resultType: Class<T>, var entityIds: MutableCollection<String> = HashSet<String>()) : AbstractList<T>() {

    companion object {
        private val log = LoggerFactory.getLogger(LazyLoadingList::class.java)
    }


    protected var cachedResults: MutableMap<Int, T> = HashMap()

    protected var countEntitiesToQueryOnDatabaseAccess = 20


    override val size: Int
        get() = entityIds.size

    override fun clear() {
        entityIds.clear()
        cachedResults.clear()
    }

    override fun get(index: Int): T? {
        cachedResults[index]?.let {
            return it
        }

        return retrieveEntityFromDatabaseAndCache(index)
    }

    protected open fun retrieveEntityFromDatabaseAndCache(index: Int): T? {
        try {
            val id = getEntityIdForIndex(index)

            val loadedEntity = entityManager.getEntityById(resultType, id)
            loadedEntity?.let { cachedResults.put(index, loadedEntity) }

            return loadedEntity
        } catch (e: Exception) {
            log.error("Could not load Result of type $resultType from Lucene search results", e)
        }

        return null
    }

    protected open fun getEntityIdForIndex(index: Int): String {
        if (entityIds is List<*> == true)
            return (entityIds as List<String>)[index]

        val iterator = entityIds.iterator()
        var i = 0
        while (iterator.hasNext()) {
            if (i == index)
                return iterator.next()

            i++
            iterator.next()
        }

        entityIds = ArrayList(entityIds) // last resort: quite a bad solution as in this way all items of entityIds will be traverse (and therefor loaded if it's a lazy  loading list
        return (entityIds as List<String>)[index]
    }


    override fun iterator(): MutableIterator<T> {
        loadAllResults()
        return super.iterator()
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        loadAllResults()
        return super.listIterator(index)
    }

    protected fun loadAllResults() {
        //    log.debug("An iterator has been called on LazyLoadingList with " + entityIds.size() + " Entity IDs, therefor all Entities will now be loaded");
        //    try { throw new Exception(); } catch(Exception ex) { log.debug("Stacktrace is:", ex); }

        if (cachedResults.size < size) {
            try {
                val allItems = entityManager.getEntitiesById(resultType, entityIds, true)

                var i = 0
                for (item in allItems) {
                    cachedResults.put(i, item)
                    i++
                }
            } catch (ex: Exception) {
                log.error("Could not retrieve all result items from Lucene search result for result type " + resultType, ex)
            }

        }
    }

    override fun add(index: Int, element: T) {
        element.id?.let { id ->
            if (entityIds is MutableList<*>) {
                (entityIds as MutableList<String>).add(index, id)
                cachedResults.put(index, element)
            } else {
                entityIds.add(id)
                cachedResults.put(cachedResults.size, element)
            }
        }
    }

    override fun remove(element: T): Boolean {
        if (cachedResults.containsValue(element)) {
            for ((index, value) in cachedResults) {
                if (element == value) {
                    cachedResults.remove(index)
                    break
                }
            }
        }

        try {
            return entityIds.remove((element as T).id)
        } catch (ex: Exception) {

        }

        return false
    }

}

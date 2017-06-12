package net.dankito.service.search.util

import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.BaseEntity
import org.slf4j.LoggerFactory
import java.util.*


open class LazyLoadingList<T : BaseEntity>(protected var entityManager: IEntityManager, protected var resultType: Class<T>, protected var entityIds: MutableCollection<String> = HashSet<String>()) : AbstractList<T>() {

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
        if (cachedResults.containsKey(index))
            return cachedResults[index]

        try {
            //      Long id = getEntityIdForIndex(index);
            //
            //      T item = Application.getEntityManager().getEntityById(resultType, id);
            //      cachedResults.put(index, item);
            //
            //      return item;

            val startTime = Date().time
            val idsOfNextEntities = getNextEntityIdsForIndex(index, countEntitiesToQueryOnDatabaseAccess)

            for (i in idsOfNextEntities.indices) {
                val item = findItemById(entityManager.getEntitiesById(resultType, idsOfNextEntities, false), idsOfNextEntities[i])
                if (item != null)
                    cachedResults.put(index + i, item)
            }

            val elapsed = Date().time - startTime
            log.debug("Preloaded {} Entities in {} milliseconds", idsOfNextEntities.size, elapsed)
            return cachedResults[index]
        } catch (ex: Exception) {
            log.error("Could not load Result of type $resultType from Lucene search results", ex)
        }

        return null
    }

    protected fun findItemById(entities: List<T>, id: String): T? {
        for (entity in entities) {
            if (id == entity.id)
                return entity
        }

        return null
    }

    protected fun getEntityIdForIndex(index: Int): String {
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

    protected fun getNextEntityIdsForIndex(index: Int, maxCountIdsToReturn: Int): List<String> {
        val ids = ArrayList<String>()

        for (i in index..(if (index + maxCountIdsToReturn < size) index + maxCountIdsToReturn else size) - 1)
            ids.add(getEntityIdForIndex(i))

        return ids
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

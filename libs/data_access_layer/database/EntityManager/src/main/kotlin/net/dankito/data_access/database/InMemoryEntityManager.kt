package net.dankito.data_access.database

import net.dankito.deepthought.model.BaseEntity
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.utils.AsyncProducerConsumerQueue
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList


class InMemoryEntityManager : IEntityManager {

    private val entitiesStore = ConcurrentHashMap<Class<Any>, MutableMap<Any, Any>>()

    val baseEntityIdField = BaseEntity::class.java.getDeclaredField("id")


    init {
        baseEntityIdField.isAccessible = true
    }


    override fun getDatabasePath(): String {
        return "InMemory"
    }

    override fun open(configuration: EntityManagerConfiguration) {
    }

    override fun optimizeDatabase() {
    }

    override fun close() {
        entitiesStore.clear()
    }


    override fun persistEntity(entity: Any): Boolean {
        val entityClass = entity.javaClass
        var typeStore = entitiesStore[entityClass]

        if(typeStore == null) {
            typeStore = ConcurrentHashMap()
            entitiesStore.put(entityClass, typeStore)
        }

        val id = createAndSetId(entity)
        var result = typeStore.put(id, entity) != null

        result = result and cascadePersist(entity)

        return result
    }

    private fun cascadePersist(entity: Any): Boolean {
        var result = true

        if(entity is DeepThought) {
            result = result and cascadePersistDeepThought(entity)
        }
        else if(entity is Item) {
            entity.notes.forEach { result = result and persistEntity(it) }
        }
        else if(entity is Source) {
            entity.previewImage?.let { result = result and persistEntity(it) }
        }

        return result
    }

    private fun cascadePersistDeepThought(entity: DeepThought): Boolean {
        var result = true

        result = result and persistEntity(entity.localUser)
        result = result and persistEntity(entity.localDevice)
        result = result and persistEntity(entity.localSettings)

        entity.fileTypes.forEach { result = result and persistEntity(it) }
        entity.noteTypes.forEach { result = result and persistEntity(it) }

        return result
    }

    override fun updateEntity(entity: Any): Boolean {
        entitiesStore[entity.javaClass]?.let { typeStore ->
            val id = getId(entity)
            if(typeStore.containsKey(id)) {
                return typeStore.put(id, entity) != null
            }
        }

        return false
    }

    override fun updateEntities(entities: List<Any>): Boolean {
        var result = entities.isNotEmpty()

        entities.forEach {
            result = result and updateEntity(it)
        }

        return result
    }

    override fun deleteEntity(entity: Any): Boolean {
        entitiesStore[entity.javaClass]?.let { typeStore ->
            return typeStore.remove(getId(entity)) != null
        }

        return false
    }

    override fun <T> getEntityById(type: Class<T>, id: String): T? {
        entitiesStore[type as Class<Any>]?.let { typeStore ->
            return typeStore[id] as? T
        }

        return null
    }

    override fun <T> getEntitiesById(type: Class<T>, ids: Collection<String>, keepOrderingOfIds: Boolean): List<T> {
        val entities = ArrayList<T>()

        ids.forEach {
            getEntityById(type, it)?.let {
                entities.add(it)
            }
        }

        return entities
    }

    override fun <T> getAllEntitiesOfType(type: Class<T>): List<T> {
        entitiesStore[type as Class<Any>]?.let { typeStore ->
            return typeStore.values.toList() as List<T>
        }

        return ArrayList()
    }

    override fun <T> getAllEntitiesUpdatedAfter(sequenceNumber: Long, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>): Long {
        return 0
    }


    private fun createAndSetId(entity: Any): Any {
        val id = UUID.randomUUID().toString()

        if(entity is BaseEntity) {
            baseEntityIdField.set(entity, id)
        }

        return id
    }

    private fun getId(entity: Any): Any {
        if(entity is BaseEntity) {
            return entity.id!!
        }

        return entity.hashCode()
    }
}
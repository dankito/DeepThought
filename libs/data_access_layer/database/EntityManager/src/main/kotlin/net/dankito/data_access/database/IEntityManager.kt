package net.dankito.data_access.database

import net.dankito.utils.AsyncProducerConsumerQueue


interface IEntityManager {

    fun getDatabasePath(): String


    fun open(configuration: EntityManagerConfiguration)

    fun optimizeDatabase()

    fun close()


    fun persistEntity(entity: Any): Boolean

    fun updateEntity(entity: Any): Boolean
    fun updateEntities(entities: List<Any>): Boolean

    fun deleteEntity(entity: Any): Boolean

    fun <T> getEntityById(type: Class<T>, id: String): T?
    fun <T> getEntitiesById(type: Class<T>, ids: Collection<String>, keepOrderingOfIds: Boolean): List<T>
    fun <T> getAllEntitiesOfType(type: Class<T>): List<T>

    /**
     * Passes all entities that have been updated after (Couchbase database) sequenceNumber on to queue.
     * Using a AsyncProducerConsumerQueue so that retrieved entities can be consumed immediately and not loading all of them into memory first
     * which could result in an OutOfMemory exception when there are too many changes for little Android's memory.
     */
    fun <T> getAllEntitiesUpdatedAfter(sequenceNumber: Long, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>): Long

}

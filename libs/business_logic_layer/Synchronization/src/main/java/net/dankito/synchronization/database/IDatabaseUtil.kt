package net.dankito.synchronization.database

import net.dankito.jpa.entitymanager.ChangedEntity
import net.dankito.util.AsyncProducerConsumerQueue


interface IDatabaseUtil {

    /**
     * Passes all entities that have been updated after (Couchbase database) sequenceNumber on to queue.
     * Using a AsyncProducerConsumerQueue so that retrieved entities can be consumed immediately and not loading all of them into memory first
     * which could result in an OutOfMemory exception when there are too many changes for little Android's memory.
     */
    fun <T> getAllEntitiesUpdatedAfter(sequenceNumber: Long, queue: AsyncProducerConsumerQueue<ChangedEntity<T>>): Long

}
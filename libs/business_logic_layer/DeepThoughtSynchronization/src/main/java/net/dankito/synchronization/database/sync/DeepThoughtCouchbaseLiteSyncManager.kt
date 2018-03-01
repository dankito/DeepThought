package net.dankito.synchronization.database.sync

import com.couchbase.lite.Database
import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.LocalSettings
import net.dankito.jpa.couchbaselite.CouchbaseLiteEntityManagerBase
import net.dankito.synchronization.database.sync.changeshandler.ISynchronizedChangesHandler
import net.dankito.synchronization.model.NetworkSettings


open class DeepThoughtCouchbaseLiteSyncManager(entityManager: CouchbaseLiteEntityManagerBase, synchronizedChangesHandler: ISynchronizedChangesHandler,
                                               networkSettings: NetworkSettings, usePushReplication: Boolean = false, usePullReplication: Boolean = true) :
        CouchbaseLiteSyncManager(entityManager, synchronizedChangesHandler, networkSettings, usePushReplication, usePullReplication) {

    override fun setReplicationFilter(database: Database) {
        entitiesToFilter.add(DeepThought::class.java.name)
        entitiesToFilter.add(LocalSettings::class.java.name)

        super.setReplicationFilter(database)
    }

}

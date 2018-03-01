package net.dankito.deepthought.android.stub

import com.couchbase.lite.Database
import net.dankito.synchronization.database.sync.changeshandler.ISynchronizedChangesHandler


class NoOpSynchronizedChangesHandler : ISynchronizedChangesHandler {

    override fun handleChange(event: Database.ChangeEvent) {
    }

}
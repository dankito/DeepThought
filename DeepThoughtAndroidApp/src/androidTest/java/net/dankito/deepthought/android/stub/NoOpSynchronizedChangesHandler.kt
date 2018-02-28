package net.dankito.deepthought.android.stub

import com.couchbase.lite.Database
import net.dankito.service.synchronization.changeshandler.ISynchronizedChangesHandler


class NoOpSynchronizedChangesHandler : ISynchronizedChangesHandler {

    override fun handleChange(event: Database.ChangeEvent) {
    }

}
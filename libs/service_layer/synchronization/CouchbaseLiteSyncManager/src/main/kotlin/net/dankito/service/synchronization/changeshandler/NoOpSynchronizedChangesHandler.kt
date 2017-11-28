package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.Database


class NoOpSynchronizedChangesHandler : ISynchronizedChangesHandler {

    override fun handleChange(event: Database.ChangeEvent) {
    }

}
package net.dankito.synchronization.database.sync.changeshandler

import com.couchbase.lite.Database


interface ISynchronizedChangesHandler {

    fun handleChange(event: Database.ChangeEvent)

}
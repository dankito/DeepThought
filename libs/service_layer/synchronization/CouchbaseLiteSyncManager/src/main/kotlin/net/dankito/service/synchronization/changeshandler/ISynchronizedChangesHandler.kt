package net.dankito.service.synchronization.changeshandler

import com.couchbase.lite.Database


interface ISynchronizedChangesHandler {

    fun handleChange(event: Database.ChangeEvent)

}
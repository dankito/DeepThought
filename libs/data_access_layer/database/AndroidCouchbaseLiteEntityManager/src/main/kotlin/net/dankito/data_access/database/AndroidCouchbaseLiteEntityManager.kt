package net.dankito.data_access.database

import com.couchbase.lite.Context
import com.couchbase.lite.android.AndroidContext


class AndroidCouchbaseLiteEntityManager(protected var androidContext: android.content.Context) : CouchbaseLiteEntityManagerBase(AndroidContext(androidContext)) {

    override fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String {
        return configuration.databaseName // TODO: what to return?
    }

}

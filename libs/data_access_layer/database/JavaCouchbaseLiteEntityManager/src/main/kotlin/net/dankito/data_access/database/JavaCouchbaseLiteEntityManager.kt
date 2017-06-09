package net.dankito.data_access.database

import com.couchbase.lite.Context

import java.io.File


class JavaCouchbaseLiteEntityManager(configuration: EntityManagerConfiguration) : CouchbaseLiteEntityManagerBase(DeepThoughtJavaContext(configuration.dataFolder)) {

    override fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String {
        // TODO: implement this in a better way as this uses implementation internal details
        return File(context.filesDir, configuration.databaseName + ".cblite2").absolutePath
    }

}

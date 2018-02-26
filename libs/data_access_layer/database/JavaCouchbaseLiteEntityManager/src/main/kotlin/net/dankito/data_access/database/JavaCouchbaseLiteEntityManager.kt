package net.dankito.data_access.database

import com.couchbase.lite.Context
import net.dankito.util.settings.ILocalSettingsStore

import java.io.File


class JavaCouchbaseLiteEntityManager(configuration: EntityManagerConfiguration, localSettingsStore: ILocalSettingsStore)
    : CouchbaseLiteEntityManagerBase(DeepThoughtJavaContext(configuration.dataFolder), localSettingsStore) {

    override fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String {
        // TODO: implement this in a better way as this uses implementation internal details
        return File(context.filesDir, configuration.databaseName + ".cblite2").absolutePath
    }

}

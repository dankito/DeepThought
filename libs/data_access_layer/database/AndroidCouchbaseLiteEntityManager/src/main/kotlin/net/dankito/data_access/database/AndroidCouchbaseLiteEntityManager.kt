package net.dankito.data_access.database

import com.couchbase.lite.Context
import com.couchbase.lite.android.AndroidContext
import net.dankito.utils.settings.ILocalSettingsStore
import java.io.File


class AndroidCouchbaseLiteEntityManager(androidContext: android.content.Context, localSettingsStore: ILocalSettingsStore)
    : CouchbaseLiteEntityManagerBase(AndroidContext(androidContext), localSettingsStore) {

    override fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String {
        return File(this.context.getFilesDir(), configuration.databaseName).absolutePath
    }

}

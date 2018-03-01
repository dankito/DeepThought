package net.dankito.jpa.couchbaselite

import com.couchbase.lite.Context
import com.couchbase.lite.android.AndroidContext
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.util.settings.ILocalSettingsStore
import java.io.File


class AndroidCouchbaseLiteEntityManager(androidContext: android.content.Context, localSettingsStore: ILocalSettingsStore)
    : DeepThoughtCouchbaseLiteEntityManagerBase(AndroidContext(androidContext), localSettingsStore) {

    override fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String {
        return File(this.context.getFilesDir(), configuration.databaseName).absolutePath
    }

}

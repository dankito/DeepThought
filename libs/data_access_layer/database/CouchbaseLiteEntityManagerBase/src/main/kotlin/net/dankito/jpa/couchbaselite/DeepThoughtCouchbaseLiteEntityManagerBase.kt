package net.dankito.jpa.couchbaselite

import com.couchbase.lite.Context
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.utils.version.Versions
import org.slf4j.LoggerFactory


abstract class DeepThoughtCouchbaseLiteEntityManagerBase(context: Context, private val localSettingsStore: ILocalSettingsStore) : CouchbaseLiteEntityManagerBase(context) {

    companion object {
        private val log = LoggerFactory.getLogger(DeepThoughtCouchbaseLiteEntityManagerBase::class.java)
    }


    override fun checkDataModelVersion() {
        val databaseDataModelVersion = localSettingsStore.getDatabaseDataModelVersion()
        val appDataModelVersion = Versions.DataModelVersion

        if(appDataModelVersion != databaseDataModelVersion) {
            adjustEntitiesToDataModelVersion(databaseDataModelVersion, appDataModelVersion)

            localSettingsStore.setDatabaseDataModelVersion(appDataModelVersion)
        }
    }

    private fun adjustEntitiesToDataModelVersion(databaseDataModelVersion: Int, appDataModelVersion: Int) {
        // implement as soon as we have first data model incompatibilities
    }

}

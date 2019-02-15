package net.dankito.utils.settings

import net.dankito.utils.version.Versions
import org.slf4j.LoggerFactory


abstract class LocalSettingsStoreBase : ILocalSettingsStore {

    companion object {
        const val DataFolderKey = "data.folder"

        const val DatabaseDataModelVersionKey = "data.model.version"

        const val SearchEngineIndexVersionKey = "search.engine.index.version"

        const val DefaultDataFolder = "data/"

        const val DefaultDatabaseDataModelVersion = Versions.DataModelVersion

        const val DefaultSearchEngineIndexVersion = Versions.SearchEngineIndexVersion


        private val log = LoggerFactory.getLogger(LocalSettingsStoreBase::class.java)
    }


    protected var DefaultDataFolder: String = Companion.DefaultDataFolder


    protected abstract fun readValueFromStore(key: String, defaultValue: String): String

    protected abstract fun saveValueToStore(key: String, value: String?)

    protected abstract fun doesValueExist(key: String): Boolean


    override fun getDataFolder(): String {
        return readStringValue(DataFolderKey, DefaultDataFolder)
    }

    override fun setDataFolder(dataFolder: String) {
        saveStringValue(DataFolderKey, dataFolder)
    }

    override fun getDatabaseDataModelVersion(): Int {
        return readIntValue(DatabaseDataModelVersionKey, DefaultDatabaseDataModelVersion)
    }

    override fun setDatabaseDataModelVersion(newDataModelVersion: Int) {
        saveIntValue(DatabaseDataModelVersionKey, newDataModelVersion)
    }

    override fun getSearchEngineIndexVersion(): Int {
        return readIntValue(SearchEngineIndexVersionKey, DefaultSearchEngineIndexVersion)
    }

    override fun setSearchEngineIndexVersion(newSearchIndexVersion: Int) {
        saveIntValue(SearchEngineIndexVersionKey, newSearchIndexVersion)
    }


    protected fun readStringValue(key: String, defaultValue: String): String {
        if(doesValueExist(key) == true) {
            return readValueFromStore(key, defaultValue)
        }
        else {
            saveValueToStore(key, defaultValue)
        }

        return defaultValue
    }

    protected fun readIntValue(key: String, defaultValue: Int): Int {
        if(doesValueExist(key) == true) {
            val value = readValueFromStore(key, Integer.toString(defaultValue))
            try {
                return Integer.parseInt(value)
            } catch (ignored: Exception) { }

        }
        else {
            saveValueToStore(key, Integer.toString(defaultValue))
        }

        return defaultValue
    }

    protected fun saveStringValue(key: String, value: String) {
        log.info("Updating '$key' to '$value'")

        saveValueToStore(key, value)
    }

    protected fun saveIntValue(key: String, value: Int) {
        saveStringValue(key, Integer.toString(value))
    }

}
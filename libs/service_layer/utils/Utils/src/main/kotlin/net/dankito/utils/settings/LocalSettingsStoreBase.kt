package net.dankito.utils.settings

import net.dankito.utils.version.Versions


abstract class LocalSettingsStoreBase : ILocalSettingsStore {

    companion object {
        const val DataFolderKey = "data.folder"

        const val DatabaseDataModelVersionKey = "data.model.version"

        const val DefaultDataFolder = "data/"

        const val DefaultDatabaseDataModelVersion = Versions.DataModelVersion
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
        saveValueToStore(key, value)
    }

    protected fun saveIntValue(key: String, value: Int) {
        saveValueToStore(key, Integer.toString(value))
    }

}
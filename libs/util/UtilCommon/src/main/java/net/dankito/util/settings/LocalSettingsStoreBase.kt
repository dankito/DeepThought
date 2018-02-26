package net.dankito.util.settings


abstract class LocalSettingsStoreBase : ILocalSettingsStore {

    companion object {
        const val DataFolderKey = "data.folder"

        const val DatabaseDataModelVersionKey = "data.model.version"

        const val DefaultDataFolder = "data/"

        const val DefaultDatabaseDataModelVersion = 1
    }


    protected var defaultDatabaseDataModelVersion = DefaultDatabaseDataModelVersion

    protected var defaultDataFolder = DefaultDataFolder


    protected abstract fun readValueFromStore(key: String, defaultValue: String): String

    protected abstract fun saveValueToStore(key: String, value: String?)

    protected abstract fun doesValueExist(key: String): Boolean


    override fun getDataFolder(): String {
        return readStringValue(DataFolderKey, defaultDataFolder)
    }

    override fun setDataFolder(dataFolder: String) {
        saveStringValue(DataFolderKey, dataFolder)
    }

    override fun getDatabaseDataModelVersion(): Int {
        return readIntValue(DatabaseDataModelVersionKey, defaultDatabaseDataModelVersion)
    }

    override fun setDatabaseDataModelVersion(newDataModelVersion: Int) {
        saveIntValue(DatabaseDataModelVersionKey, newDataModelVersion)
    }


    protected open fun readStringValue(key: String, defaultValue: String): String {
        if(doesValueExist(key)) {
            return readValueFromStore(key, defaultValue)
        }
        else {
            saveValueToStore(key, defaultValue)
        }

        return defaultValue
    }

    protected open fun readIntValue(key: String, defaultValue: Int): Int {
        if(doesValueExist(key)) {
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

    protected open fun saveStringValue(key: String, value: String) {
        saveValueToStore(key, value)
    }

    protected open fun saveIntValue(key: String, value: Int) {
        saveValueToStore(key, Integer.toString(value))
    }

}
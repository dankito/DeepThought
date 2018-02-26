package net.dankito.util.settings


interface ILocalSettingsStore {

    fun getDataFolder(): String

    fun setDataFolder(dataFolder: String)

    fun getDatabaseDataModelVersion(): Int

    fun setDatabaseDataModelVersion(newDataModelVersion: Int)

}
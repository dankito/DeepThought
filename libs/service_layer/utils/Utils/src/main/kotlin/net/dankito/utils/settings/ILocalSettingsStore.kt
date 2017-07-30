package net.dankito.utils.settings


interface ILocalSettingsStore {

    fun getDataFolder(): String

    fun setDataFolder(dataFolder: String)

    fun getDatabaseDataModelVersion(): Int

    fun setDatabaseDataModelVersion(newDataModelVersion: Int)

}
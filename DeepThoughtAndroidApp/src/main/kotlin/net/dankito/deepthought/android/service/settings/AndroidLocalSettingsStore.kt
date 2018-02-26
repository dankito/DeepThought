package net.dankito.deepthought.android.service.settings

import android.content.Context
import net.dankito.utils.settings.LocalSettingsStoreBase
import net.dankito.utils.version.Versions


class AndroidLocalSettingsStore(private val context: Context) : LocalSettingsStoreBase() {

    private val preferences = context.getSharedPreferences("DeepThoughtAndroidAppSettings", Context.MODE_PRIVATE)


    init {
        defaultDatabaseDataModelVersion = Versions.DataModelVersion
        defaultDataFolder = determineDefaultDataFolder()
    }


    override fun readValueFromStore(key: String, defaultValue: String): String {
        return preferences.getString(key, defaultValue)
    }

    override fun saveValueToStore(key: String, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)

        editor.apply()
    }

    override fun doesValueExist(key: String): Boolean {
        return preferences.contains(key)
    }


    private fun determineDefaultDataFolder(): String {
        // TODO: use AndroidFileStorageService to get data folder
        val dataFolderFile = context.getDir("LocalPreferences", Context.MODE_PRIVATE)

        if(dataFolderFile.exists() == false) {
            dataFolderFile.mkdirs()
        }

        return dataFolderFile.absolutePath + "/"
    }

}

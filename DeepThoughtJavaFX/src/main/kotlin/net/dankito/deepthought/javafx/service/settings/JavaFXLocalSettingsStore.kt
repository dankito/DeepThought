package net.dankito.deepthought.javafx.service.settings

import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.settings.LocalSettingsStoreBase
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*


class JavaFXLocalSettingsStore(private val platformConfiguration: IPlatformConfiguration) : LocalSettingsStoreBase() {

    companion object {
        val DeepThoughtPropertiesFileName = "DeepThoughtFx.properties"

        private val log = LoggerFactory.getLogger(JavaFXLocalSettingsStore::class.java)
    }


    private var deepThoughtProperties: Properties? = null

    private var doesPropertiesFileExist: Boolean? = null


    init {
        deepThoughtProperties = loadDeepThoughtProperties()
    }


    private fun loadDeepThoughtProperties(): Properties? {
        val propertiesFile = getPropertiesFile()

        try {
            val deepThoughtProperties = Properties()

            deepThoughtProperties.load(InputStreamReader(FileInputStream(propertiesFile), "UTF-8"))
            doesPropertiesFileExist = true

            return deepThoughtProperties
        }
        catch (e: Exception) {
            if(e is FileNotFoundException) { // on first app start
                return tryToCreateSettingsFile()
            }
            else {
                log.warn("Could not load data folder from $propertiesFile", e)
            }
        }

        doesPropertiesFileExist = false
        return null
    }

    private fun saveDeepThoughtProperties() {
        saveDeepThoughtProperties(deepThoughtProperties)
    }

    private fun saveDeepThoughtProperties(deepThoughtProperties: Properties?): Boolean {
        val propertiesFile = getPropertiesFile()

        try {
            if(deepThoughtProperties != null) {
                deepThoughtProperties.store(OutputStreamWriter(FileOutputStream(propertiesFile), "UTF-8"), null)
                return true
            }
        }
        catch(ex: Exception) {
            log.error("Could not save DeepThoughtProperties to $propertiesFile", ex)
        }

        return false
    }

    private fun getPropertiesFile(): File {
        return File(platformConfiguration.getApplicationFolder(), DeepThoughtPropertiesFileName)
    }

    private fun tryToCreateSettingsFile(): Properties? {
        val newFile = Properties()

        if(saveDeepThoughtProperties(newFile)) {
            doesPropertiesFileExist = true
            return newFile
        }

        return null
    }

    override fun readValueFromStore(key: String, defaultValue: String): String {
        deepThoughtProperties?.let {
            return it.getProperty(key, defaultValue)
        }

        return defaultValue
    }

    override fun saveValueToStore(key: String, value: String?) {
        var value = value

        deepThoughtProperties?.let { properties ->
            if(value == null) {
                value = ""
            }

            properties.put(key, value)

            saveDeepThoughtProperties()
        }
    }

    override fun doesValueExist(key: String): Boolean {
        deepThoughtProperties?.let {
            return it.containsKey(key)
        }

        return false
    }

}

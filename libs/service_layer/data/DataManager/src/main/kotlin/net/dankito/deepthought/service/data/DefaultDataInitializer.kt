package net.dankito.deepthought.service.data

import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.LocalSettings
import net.dankito.deepthought.model.User
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.localization.Localization
import net.dankito.utils.version.Versions
import org.slf4j.LoggerFactory
import java.util.*


open class DefaultDataInitializer(private val platformConfiguration: IPlatformConfiguration, private val localization: Localization) {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultDataInitializer::class.java)
    }


    fun createDefaultData(): DeepThought {
        val localUser = createNewLocalUser()

        val localDevice = createUserDefaultDevice(localUser)

        val localSettings = LocalSettings(Versions.CommunicationProtocolVersion, Versions.SearchEngineIndexVersion, Versions.HtmlEditorVersion, Date(0), 0, Date(0))

        val deepThought = DeepThought(localUser, localDevice, localSettings)

        return deepThought
    }


    protected open fun createNewLocalUser(): User {
        val universallyUniqueId = UUID.randomUUID().toString()
        var userName: String = getLocalizedString("default.user.name")

        try {
            userName = platformConfiguration.getUserName()
        } catch (ex: Exception) {
            log.error("Could not get System property user.name", ex)
        }

        val user = User(userName, universallyUniqueId)

        return user
    }

    protected open fun createUserDefaultDevice(user: User): Device {
        val universallyUniqueId = UUID.randomUUID().toString()

        val osVersion = platformConfiguration.getOsVersionString()
        val osName = platformConfiguration.getOsName()

        var deviceName = getLocalizedString("users.default.device.name", user.userName, osName)
        platformConfiguration.getDeviceName()?.let { deviceName = it }

        val userDefaultDevice = Device(deviceName, universallyUniqueId, platformConfiguration.getOsType(),
                osName, osVersion)

        return userDefaultDevice
    }


    private fun  getLocalizedString(resourceKey: String, vararg parameter: String): String {
        return localization.getLocalizedString(resourceKey, *parameter)
    }

}
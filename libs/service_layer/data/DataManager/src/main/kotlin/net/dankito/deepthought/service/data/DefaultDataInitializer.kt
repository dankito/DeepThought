package net.dankito.deepthought.service.data

import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.User
import net.dankito.deepthought.model.enums.ApplicationLanguage
import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.model.enums.FileTypeDefaultFolderName
import net.dankito.deepthought.model.enums.NoteType
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.localization.Localization
import org.slf4j.LoggerFactory
import java.util.*


open class DefaultDataInitializer(private val platformConfiguration: IPlatformConfiguration, private val localization: Localization) {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultDataInitializer::class.java)
    }


    fun createDefaultData(): DeepThought {
        val localUser = createNewLocalUser()

        val localDevice = createUserDefaultDevice(localUser)

        val deepThought = initDeepThought(localUser, localDevice)

        createEnumerationsDefaultValues(deepThought)

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


    private fun initDeepThought(defaultLocalUser: User, localDevice: Device): DeepThought {
        val deepThought = DeepThought(defaultLocalUser, localDevice)

        deepThought.addUser(defaultLocalUser)

        deepThought.addDevice(localDevice)

        return deepThought
    }


    protected open fun createEnumerationsDefaultValues(deepThought: DeepThought) {
        createDefaultApplicationLanguages(deepThought)
        createDefaultNoteTypes(deepThought)
        createDefaultFileTypes(deepThought)
    }

    protected open fun createDefaultApplicationLanguages(deepThought: DeepThought) {
        deepThought.addApplicationLanguage(ApplicationLanguage("application.language.english", "en", true, 1))
        deepThought.addApplicationLanguage(ApplicationLanguage("application.language.german", "de", true, 2))
    }

    protected open fun createDefaultNoteTypes(deepThought: DeepThought) {
        deepThought.addNoteType(NoteType("note.type.unset", true, 1))
        deepThought.addNoteType(NoteType("note.type.comment", true, 2))
        deepThought.addNoteType(NoteType("note.type.info", true, 3))
        deepThought.addNoteType(NoteType("note.type.to.do", true, 4))
        deepThought.addNoteType(NoteType("note.type.thought", true, 5))
    }

    protected open fun createDefaultFileTypes(deepThought: DeepThought) {
        deepThought.addFileType(FileType("file.type.document", FileTypeDefaultFolderName.Documents.folderName, true, 1))
        deepThought.addFileType(FileType("file.type.image", FileTypeDefaultFolderName.Images.folderName, true, 2))
        deepThought.addFileType(FileType("file.type.audio", FileTypeDefaultFolderName.Audio.folderName, true, 3))
        deepThought.addFileType(FileType("file.type.video", FileTypeDefaultFolderName.Video.folderName, true, 4))
        deepThought.addFileType(FileType("file.type.other.files", FileTypeDefaultFolderName.OtherFilesFolderName.folderName, true, Integer.MAX_VALUE))
    }


    private fun  getLocalizedString(resourceKey: String, vararg parameter: String): String {
        return localization.getLocalizedString(resourceKey, *parameter)
    }

}
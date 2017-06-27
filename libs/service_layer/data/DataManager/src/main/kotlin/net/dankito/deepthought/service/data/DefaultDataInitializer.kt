package net.dankito.deepthought.service.data

import net.dankito.deepthought.model.DeepThought
import net.dankito.deepthought.model.DeepThoughtApplication
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.User
import net.dankito.utils.IPlatformConfiguration
import org.slf4j.LoggerFactory
import java.util.*


open class DefaultDataInitializer(private val platformConfiguration: IPlatformConfiguration) {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultDataInitializer::class.java)
    }


    fun createDefaultData(): DeepThoughtApplication {
        val defaultLocalUser = createNewLocalUser()

        val localDevice = createUserDefaultDevice(defaultLocalUser)
        defaultLocalUser.addDevice(localDevice)

        val defaultDeepThought = createEmptyDeepThought()
        defaultLocalUser.lastViewedDeepThought = defaultDeepThought

        val application = initDeepThoughtApplication(defaultLocalUser, localDevice)

        createEnumerationsDefaultValues(defaultDeepThought)

        return application
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


    protected open fun createEmptyDeepThought(): DeepThought {
        val emptyDeepThought = DeepThought()

        createEnumerationsDefaultValues(emptyDeepThought)

        return emptyDeepThought
    }


    private fun initDeepThoughtApplication(defaultLocalUser: User, localDevice: Device): DeepThoughtApplication {
        val application = DeepThoughtApplication(defaultLocalUser, localDevice, true)

        application.addUser(defaultLocalUser)

        for (device in defaultLocalUser.devices) {
            application.addDevice(device)
        }
        return application
    }


    protected open fun createEnumerationsDefaultValues(deepThought: DeepThought) {
        createNoteTypeDefaultValues(deepThought)
        createFileTypeDefaultValues(deepThought)
    }

    protected open fun createNoteTypeDefaultValues(deepThought: DeepThought) {
//        deepThought.addNoteType(NoteType("note.type.unset", true, false, 1))
//        deepThought.addNoteType(NoteType("note.type.comment", true, false, 2))
//        deepThought.addNoteType(NoteType("note.type.info", true, false, 3))
//        deepThought.addNoteType(NoteType("note.type.to.do", true, false, 4))
//        deepThought.addNoteType(NoteType("note.type.thought", true, false, 5))
    }

    protected open fun createFileTypeDefaultValues(deepThought: DeepThought) {
//        deepThought.addFileType(FileType("file.type.other.files", FileUtils.OtherFilesFolderName, true, false, Integer.MAX_VALUE))
//        deepThought.addFileType(FileType("file.type.document", FileUtils.DocumentsFilesFolderName, true, true, 1))
//        deepThought.addFileType(FileType("file.type.image", FileUtils.ImagesFilesFolderName, true, false, 2))
//        deepThought.addFileType(FileType("file.type.audio", FileUtils.AudioFilesFolderName, true, true, 3))
//        deepThought.addFileType(FileType("file.type.video", FileUtils.VideoFilesFolderName, true, true, 4))
    }


    private fun  getLocalizedString(resourceKey: String, vararg parameter: String): String {
        return resourceKey // TODO
    }

}
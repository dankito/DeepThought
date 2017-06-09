package net.dankito.deepthought.service.data

import net.dankito.deepthought.model.*
import org.slf4j.LoggerFactory
import java.util.*


open class DefaultDataInitializer {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultDataInitializer::class.java)
    }


    fun createDefaultData(): DeepThoughtApplication {
        val defaultLocalUser = createNewLocalUser()

        val localDevice = createUserDefaultDevice(defaultLocalUser)
        defaultLocalUser.addDevice(localDevice)

        val defaultDeepThought = createEmptyDeepThought()
        defaultLocalUser.addDeepThought(defaultDeepThought)
        defaultLocalUser.lastViewedDeepThought = defaultDeepThought

        val application = initDeepThoughtApplication(defaultLocalUser, localDevice)

        createEnumerationsDefaultValues(defaultDeepThought)

        return application
    }


    protected open fun createNewLocalUser(): User {
        val universallyUniqueId = UUID.randomUUID().toString()
        var userName: String = getLocalizedString("default.user.name")

        try {
            // TODO
//            userName = Application.getPlatformConfiguration().getUserName()
            userName = "Manfred"
        } catch (ex: Exception) {
            log.error("Could not get System property user.name", ex)
        }

        val userGroup = createUserDefaultGroup(userName)

        val user = User(userName, universallyUniqueId, true, userGroup)

        return user
    }

    protected open fun createUserDefaultGroup(userName: String): UsersGroup {
        val universallyUniqueId = UUID.randomUUID().toString()

        return UsersGroup(getLocalizedString("users.group", userName), universallyUniqueId)
    }

    protected open fun createUserDefaultDevice(user: User): Device {
        val universallyUniqueId = UUID.randomUUID().toString()

        var platform = System.getProperty("os.name")
        var deviceName = getLocalizedString("users.default.device.name", user.userName, platform)
        var osVersion = System.getProperty("os.version")

        // TODO
//        if (Application.getPlatformConfiguration() != null) { // TODO: try to get rid of static method calls
//            osVersion = Application.getPlatformConfiguration().getOsVersionString()
//            platform = Application.getPlatformConfiguration().getPlatformName()
//
//            if (Application.getPlatformConfiguration().getDeviceName() != null) {
//                deviceName = Application.getPlatformConfiguration().getDeviceName()
//            }
//        }

        val userDefaultDevice = Device(deviceName, universallyUniqueId,
                platform, osVersion, System.getProperty("os.arch"))

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

        for (group in defaultLocalUser.groups) {
            application.addGroup(group)
            localDevice.addGroup(group)
        }

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
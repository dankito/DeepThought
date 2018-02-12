package net.dankito.deepthought.javafx.service.settings

import net.dankito.deepthought.model.enums.OsType
import net.dankito.utils.PlatformConfigurationBase
import java.io.File

class JavaFXPlatformConfiguration : PlatformConfigurationBase() {

    override fun getUserName(): String {
        return System.getProperty("user.name")
    }

    override fun getDeviceName(): String? {
        return null
    }

    override fun getOsType(): OsType {
        return OsType.DESKTOP
    }

    override fun getOsName(): String {
        return System.getProperty("os.name")
    }

    override fun getOsVersion(): Int {
        // TODO: don't know how to do this generically on JavaSE (but this information is anyway right now only used for Android)
        return 0
    }

    override fun getOsVersionString(): String {
        return System.getProperty("os.version")
    }


    override fun getApplicationFolder(): File {
        return File(System.getProperty("user.dir")).absoluteFile
    }

    override fun getDefaultDataFolder(): File {
        return ensureFolderExists(File(getApplicationFolder(), "data"))
    }

    override fun getDefaultFilesFolder(): File {
        return ensureFolderExists(File(getApplicationFolder(), "files"))
    }

}

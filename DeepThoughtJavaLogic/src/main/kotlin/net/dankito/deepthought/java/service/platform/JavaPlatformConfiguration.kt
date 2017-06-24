package net.dankito.deepthought.java.service.platform

import net.dankito.deepthought.model.OsType
import net.dankito.utils.IPlatformConfiguration
import java.io.File

class JavaPlatformConfiguration : IPlatformConfiguration {

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


    override fun getDefaultDataFolder(): File {
        return File("data")
    }

}

package net.dankito.deepthought.android.service.platform

import android.content.Context
import android.os.Build
import net.dankito.utils.IPlatformConfiguration
import java.io.File


class AndroidPlatformConfiguration(val context: Context) : IPlatformConfiguration {

    override fun getUserName(): String {
        // here's a hint how it can be done but in my eyes it's not worth the effort: https://stackoverflow.com/questions/9323207/how-can-i-get-the-first-name-or-full-name-of-the-user-of-the-phone
        return System.getProperty("user.name")
    }

    override fun getDeviceName(): String? {
        var manufacturer = Build.MANUFACTURER
        if (manufacturer.isNotEmpty() && Character.isLowerCase(manufacturer[0])) {
            manufacturer = Character.toUpperCase(manufacturer[0]) + manufacturer.substring(1)
        }

        return manufacturer + " " + Build.MODEL
    }

    override fun getPlatformName(): String {
        return "Android"
    }

    override fun getOsVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    override fun getOsVersionString(): String {
        return Build.VERSION.RELEASE
    }


    override fun getDefaultDataFolder(): File {
        val dataFolderFile = context.getDir("data", Context.MODE_PRIVATE)

        if (dataFolderFile.exists() == false) {
            dataFolderFile.mkdirs()
        }

        return dataFolderFile
    }

}

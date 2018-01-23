package net.dankito.deepthought.android.service.settings

import android.content.Context
import android.os.Build
import android.os.Environment
import net.dankito.deepthought.model.enums.OsType
import net.dankito.utils.IPlatformConfiguration
import org.slf4j.LoggerFactory
import java.io.File


class AndroidPlatformConfiguration(val context: Context) : IPlatformConfiguration {

    companion object {
        private val log = LoggerFactory.getLogger(AndroidPlatformConfiguration::class.java)
    }


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

    override fun getOsType(): OsType {
        return OsType.ANDROID
    }

    override fun getOsName(): String {
        return "Android"
    }

    override fun getOsVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    override fun getOsVersionString(): String {
        return Build.VERSION.RELEASE
    }


    override fun getApplicationFolder(): File {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            // app data dir
//            return File(packageInfo.applicationInfo.dataDir)

            // apk dir
            return File(packageInfo.applicationInfo.sourceDir).parentFile // or publicSourceDir ?
        } catch (e: Exception) {
            log.error("Could not get app's data dir", e)
        }

        return File("/")
    }

    override fun getDefaultDataFolder(): File {
        val dataFolderFile = context.getDir("data", Context.MODE_PRIVATE)

        if(dataFolderFile.exists() == false) {
            dataFolderFile.mkdirs()
        }

        return dataFolderFile
    }

    override fun getDefaultFilesFolder(): File {
        // saving files to internal data dir is no alternative as then viewer applications cannot access files
        return File(Environment.getExternalStorageDirectory(), "DeepThought") // TODO: what to do if user didn't give permission yet to access external storage?
    }

}

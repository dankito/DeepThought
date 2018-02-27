package net.dankito.utils

import net.dankito.synchronization.model.enums.FileType
import net.dankito.synchronization.model.enums.OsType
import java.io.File


interface IPlatformConfiguration {

    fun getUserName(): String

    fun getDeviceName(): String?

    fun getOsType(): OsType

    fun getOsName(): String

    fun getOsVersion(): Int

    fun getOsVersionString(): String


    fun getApplicationFolder(): File

    fun getDefaultDataFolder(): File

    fun getDefaultFilesFolder(): File

    fun getDefaultSavePathForFile(filename: String, fileType: FileType): File

}

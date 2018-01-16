package net.dankito.utils

import net.dankito.deepthought.model.enums.OsType
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

}

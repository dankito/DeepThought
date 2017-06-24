package net.dankito.utils

import net.dankito.deepthought.model.OsType
import java.io.File


interface IPlatformConfiguration {

    fun getUserName(): String

    fun getDeviceName(): String?

    fun getOsType(): OsType

    fun getOsName(): String

    fun getOsVersion(): Int

    fun getOsVersionString(): String


    fun getDefaultDataFolder(): File

}

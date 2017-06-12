package net.dankito.utils

import java.io.File


interface IPlatformConfiguration {

    fun getUserName(): String

    fun getDeviceName(): String?

    fun getPlatformName(): String

    fun getOsVersion(): Int

    fun getOsVersionString(): String


    fun getDefaultDataFolder(): File

}

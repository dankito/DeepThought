package net.dankito.deepthought.android.util

import android.os.Build


object OsHelper {

    val isRunningOnAndroid = determineIfIsRunningOnAndroid()


    fun isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(minimumApiLevel: Int): Boolean {
        return isRunningOnAndroid == false || isRunningOnAndroidAtLeastOfApiLevel(minimumApiLevel)
    }

    fun isRunningOnAndroidApiLevel(apiLevel: Int): Boolean {
        return isRunningOnAndroid && getAndroidOsVersion() == apiLevel
    }

    fun isRunningOnAndroidAtLeastOfApiLevel(minimumApiLevel: Int): Boolean {
        return isRunningOnAndroid && getAndroidOsVersion() >= minimumApiLevel
    }

    fun getAndroidOsVersion(): Int {
        return Build.VERSION.SDK_INT
    }


    private fun determineIfIsRunningOnAndroid(): Boolean {
        try {
            Class.forName("android.app.Activity")
            return true
        } catch (ex: Exception) {
        }

        return false
    }

}

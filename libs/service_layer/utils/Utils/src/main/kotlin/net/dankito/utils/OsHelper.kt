package net.dankito.utils


class OsHelper(private val platformConfiguration: IPlatformConfiguration) {

    val isRunningOnAndroid = determineIfIsRunningOnAndroid()


    fun isRunningOnJavaSeOrOnAndroidApiLevelAtLeastOf(minimumApiLevel: Int): Boolean {
        return isRunningOnAndroid == false || isRunningOnAndroidAtLeastOfApiLevel(minimumApiLevel)
    }

    fun isRunningOnAndroidApiLevel(apiLevel: Int): Boolean {
        return isRunningOnAndroid && getOsVersion() == apiLevel
    }

    fun isRunningOnAndroidAtLeastOfApiLevel(minimumApiLevel: Int): Boolean {
        return isRunningOnAndroid && getOsVersion() >= minimumApiLevel
    }

    fun getOsVersion(): Int {
        return platformConfiguration.getOsVersion()
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

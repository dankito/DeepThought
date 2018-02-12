package net.dankito.utils

import java.io.File


abstract class PlatformConfigurationBase : IPlatformConfiguration {

    protected fun ensureFolderExists(folder: File): File {
        if(folder.exists() == false) {
            folder.mkdirs()
        }

        return folder
    }

}
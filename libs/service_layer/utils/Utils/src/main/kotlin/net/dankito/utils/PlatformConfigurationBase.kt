package net.dankito.utils

import java.io.File


abstract class PlatformConfigurationBase : IPlatformConfiguration {

    companion object {
        const val DataFolderName = "data"
    }


    protected fun ensureFolderExists(folder: File): File {
        if(folder.exists() == false) {
            folder.mkdirs()
        }

        return folder
    }

}
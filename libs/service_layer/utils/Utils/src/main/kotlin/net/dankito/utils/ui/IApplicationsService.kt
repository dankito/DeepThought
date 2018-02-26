package net.dankito.utils.ui

import java.io.File


interface IApplicationsService {

    fun openFileInOsDefaultApplication(file: File)

    fun openDirectoryInOsFileBrowser(file: File)

}
package net.dankito.utils.ui

import net.dankito.deepthought.model.FileLink
import java.io.File


interface IApplicationsService {

    fun openFileInOsDefaultApplication(file: FileLink)

    fun openDirectoryInOsFileBrowser(file: File)

}
package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.data.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.utils.ui.IApplicationsService
import java.io.File


class FileListPresenter(private val fileManager: FileManager, private val applicationsService: IApplicationsService) {

    fun showFile(file: FileLink) {
        applicationsService.openFileInOsDefaultApplication(file)
    }

    fun openContainingDirectoryOfFile(file: FileLink) {
        val absolutePath = fileManager.getLocalPathForFile(file)
        openDirectory(absolutePath.parentFile)
    }

    fun openDirectory(directory: File) {
        applicationsService.openDirectoryInOsFileBrowser(directory)
    }

}
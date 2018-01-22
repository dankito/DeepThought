package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.utils.ui.IApplicationsService
import java.io.File


class FileListPresenter(private val fileManager: FileManager, private val applicationsService: IApplicationsService) {

    fun showFile(file: FileLink) {
        // TODO: check if file can be opened in DeepThought directly, e.g PDFs
        applicationsService.openFileInOsDefaultApplication(file)
    }

    fun openContainingDirectoryOfFile(file: FileLink) {
        val absolutePath = fileManager.getLocalPathForFile(file)
        openDirectory(absolutePath.parentFile)
    }

    fun openDirectory(directory: File) {
        applicationsService.openDirectoryInOsFileBrowser(directory)
    }


    fun ensureLocalFileInfoIsSet(files: Collection<FileLink>) {
        fileManager.ensureLocalFileInfoIsSetAndMayStartSynchronization(files)
    }


    fun formatFileSize(length: Long): String {
        if (length > 0.1 * 1024.0 * 1024.0 * 1024.0) {
            val f = length.toFloat() / 1024f / 1024f / 1024f
            return String.format("%.1f GB", f)
        }
        else if (length > 0.1 * 1024.0 * 1024.0) {
            val f = length.toFloat() / 1024f / 1024f
            return String.format("%.1f MB", f)
        }
        else {
            val f = length / 1024f
            return String.format("%.1f kB", f)
        }
    }

}
package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IApplicationsService
import java.io.File


class FileListPresenter(private val fileManager: FileManager, private val applicationsService: IApplicationsService, private val localization: Localization) {

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


    fun forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(files: Collection<FileLink>) {
        fileManager.forLocalFilesEnsureLocalFileInfoIsSetAndMayStartSynchronization(files)
    }


    fun formatFileSize(length: Long): String {
        if(length > 1024.0 * 1024.0 * 1024.0) {
            val f = length.toFloat() / 1024f / 1024f / 1024f
            return String.format("%.1f GB", f)
        }
        else if(length > 1024.0 * 1024.0) {
            val f = length.toFloat() / 1024f / 1024f
            return String.format("%.1f MB", f)
        }
        else if(length < 0) {
            return localization.getLocalizedString("file.size.not.determined.yet")
        }
        else {
            val f = length / 1024f
            return String.format("%.1f kB", f)
        }
    }

    fun getUriOrSynchronizationState(file: FileLink): String {
        if(file.isLocalFile == false) {
            return file.uriString
        }
        else {
            val localFileInfo = file.localFileInfo
            val localPath = localFileInfo?.path

            if(localFileInfo != null && localFileInfo.syncStatus == FileSyncStatus.UpToDate && localPath != null) {
                return localPath
            }
            else {
                return localization.getLocalizedString("file.sync.status.not.synchronized.yet")
            }
        }
    }

}
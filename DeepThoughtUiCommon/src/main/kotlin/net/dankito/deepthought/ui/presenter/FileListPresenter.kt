package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IApplicationsService
import java.io.File


class FileListPresenter(private val fileManager: FileManager, private val applicationsService: IApplicationsService, private val localization: Localization,
                        private val router: IRouter) {

    fun showFile(file: FileLink, sourceForFile: Source? = null) {
        // TODO: check if file can be opened in DeepThought directly, e.g PDFs
        if(isPdfFile(file)) { // TODO: remove for 0.4.0 release
            router.showPdfView(file, sourceForFile)
        }
        else {
            applicationsService.openFileInOsDefaultApplication(file)
        }
    }

    fun openContainingDirectoryOfFile(file: FileLink) {
        fileManager.getLocalPathForFile(file)?.let { absolutePath ->
            openDirectory(absolutePath.parentFile)
        }
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
            val localFileInfo = getLocalFileInfo(file)
            val localPath = localFileInfo?.path

            if(localFileInfo != null && localFileInfo.syncStatus == FileSyncStatus.UpToDate && localPath != null) {
                return File(localPath).absolutePath
            }
            else {
                return localization.getLocalizedString("file.sync.status.not.synchronized.yet")
            }
        }
    }

    fun getLocalFileInfo(file: FileLink): LocalFileInfo? {
        return fileManager.getStoredLocalFileInfo(file)
    }


    private fun isPdfFile(file: FileLink): Boolean {
        getLocalFileInfo(file)?.path?.let { localFilePath ->
            return "pdf".equals(File(localFilePath).extension, true) // TODO: find better way
        }

        return false
    }

}
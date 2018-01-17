package net.dankito.deepthought.javafx.service

import net.dankito.deepthought.data.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.utils.ui.IApplicationsService
import java.awt.Desktop
import java.io.File
import kotlin.concurrent.thread


class JavaFXApplicationsService(private val fileManager: FileManager) : IApplicationsService {

    override fun openFileInOsDefaultApplication(file: FileLink) {
        val absoluteFile = fileManager.getLocalPathForFile(file)

        openFile(absoluteFile)
    }

    override fun openDirectoryInOsFileBrowser(file: File) {
        openFile(file)
    }

    private fun openFile(file: File) {
        thread { // get off UI thread
            try {
                Desktop.getDesktop().open(file)
            } catch(ignored: Exception) { }
        }
    }

}
package net.dankito.deepthought.javafx.service

import net.dankito.util.IThreadPool
import net.dankito.utils.ui.IApplicationsService
import java.awt.Desktop
import java.io.File


class JavaFXApplicationsService(private val threadPool: IThreadPool) : IApplicationsService {

    override fun openFileInOsDefaultApplication(file: File) {
        openFileOffUiThread(file)
    }

    override fun openDirectoryInOsFileBrowser(file: File) {
        openFileOffUiThread(file)
    }

    private fun openFileOffUiThread(file: File) {
        threadPool.runAsync { // get off UI thread
            try {
                Desktop.getDesktop().open(file)
            } catch(ignored: Exception) { }
        }
    }

}
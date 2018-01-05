package net.dankito.deepthought.android.dialogs

import android.content.Context
import android.os.Environment
import yogesh.firzen.filelister.FileListerDialog
import java.io.File


class FileChooserDialog(private val applicationContext: Context) {

    private var lastSelectedFolder: File = Environment.getExternalStorageDirectory()


    fun selectFile(initialFolder: File? = lastSelectedFolder, fileSelected: (File) -> Unit) {
        val fileListerDialog = FileListerDialog.createFileListerDialog(applicationContext)
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.ALL_FILES)

        initialFolder?.let { fileListerDialog.setDefaultDir(it) }

        fileListerDialog.setOnFileSelectedListener { file, path ->
            lastSelectedFolder = file.parentFile

            fileSelected(file)
        }

        fileListerDialog.show()
    }

}
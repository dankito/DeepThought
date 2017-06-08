package net.dankito.data_access.filesystem

import android.content.Context
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream


class AndroidFileStorageService(val context: Context) : JavaFileStorageService() {


    override fun getFileInDataFolder(filename: String): File {
        return File(context.filesDir, filename)
    }


    @Throws(FileNotFoundException::class)
    override fun createFileOutputStream(filename: String): OutputStream {
        return context.openFileOutput(filename, Context.MODE_PRIVATE)
    }

    @Throws(FileNotFoundException::class)
    override fun createFileInputStream(filename: String): InputStream {
        return context.openFileInput(filename)
    }

}
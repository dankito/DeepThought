package net.dankito.data_access.filesystem

import java.io.*


open class JavaFileStorageService : IFileStorageService {

    companion object {
        private val DATA_FOLDER_NAME = "data"
    }


    override fun getFileInDataFolder(filename: String) : File {
        return File(DATA_FOLDER_NAME, filename)
    }


    @Throws(Exception::class)
    override fun writeToTextFile(fileContent: String, filename: String) {
        val outputStreamWriter = OutputStreamWriter(createFileOutputStream(filename))

        outputStreamWriter.write(fileContent)

        outputStreamWriter.flush()
        outputStreamWriter.close()
    }

    @Throws(Exception::class)
    override fun writeToBinaryFile(fileContent: ByteArray, filename: String) {
        val outputStream = createFileOutputStream(filename)

        outputStream.write(fileContent)

        outputStream.flush()
        outputStream.close()
    }

    @Throws(FileNotFoundException::class)
    override fun createFileOutputStream(filename: String): OutputStream {
        return FileOutputStream(filename)
    }


    @Throws(Exception::class)
    override fun readFromTextFile(filename: String): String? {
        val inputStream = createFileInputStream(filename)

        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        val fileContent = bufferedReader.use { it.readLines() }.joinToString(separator = "") { it }

        bufferedReader.close()
        inputStream.close()

        return fileContent
    }

    @Throws(Exception::class)
    override fun readFromBinaryFile(filename: String): ByteArray? {
        val inputStream = createFileInputStream(filename)

        val buffer = ByteArrayOutputStream()

        inputStream.copyTo(buffer, 16384)

        buffer.flush()
        inputStream.close()

        return buffer.toByteArray()
    }

    @Throws(FileNotFoundException::class)
    override fun createFileInputStream(filename: String): InputStream {
        return FileInputStream(filename)
    }


    override fun deleteFolderRecursively(path: String) {
        deleteRecursively(File(path))
    }

    protected fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            for (containingFile in file.listFiles()!!) {
                deleteRecursively(containingFile)
            }
        }

        file.delete()
    }

}
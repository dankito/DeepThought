package net.dankito.data_access.filesystem

import java.io.*


open class JavaFileStorageService : IFileStorageService {

    companion object {
        private const val DATA_FOLDER_NAME = "data"
    }


    override fun getFileInDataFolder(filename: String) : File {
        return File(DATA_FOLDER_NAME, filename)
    }


    @Throws(Exception::class)
    override fun writeToTextFile(fileContent: String, file: File) {
        val outputStreamWriter = OutputStreamWriter(createFileOutputStream(file))

        outputStreamWriter.write(fileContent)

        outputStreamWriter.flush()
        outputStreamWriter.close()
    }

    @Throws(Exception::class)
    override fun writeToBinaryFile(fileContent: ByteArray, file: File) {
        val outputStream = createFileOutputStream(file)

        outputStream.write(fileContent)

        outputStream.flush()
        outputStream.close()
    }

    @Throws(FileNotFoundException::class)
    override fun createFileOutputStream(file: File): OutputStream {
        return FileOutputStream(file)
    }


    @Throws(Exception::class)
    override fun readFromTextFile(file: File): String? {
        val inputStream = createFileInputStream(file)

        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        val fileContent = bufferedReader.use { it.readLines() }.joinToString(separator = "") { it }

        bufferedReader.close()
        inputStream.close()

        return fileContent
    }

    @Throws(Exception::class)
    override fun readFromBinaryFile(file: File): ByteArray? {
        val inputStream = createFileInputStream(file)

        val buffer = ByteArrayOutputStream()

        inputStream.copyTo(buffer, 16384)

        buffer.flush()
        inputStream.close()

        return buffer.toByteArray()
    }

    @Throws(FileNotFoundException::class)
    override fun createFileInputStream(file: File): InputStream {
        return FileInputStream(file)
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
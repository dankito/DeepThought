package net.dankito.util.filesystem

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream


interface IFileStorageService {

    fun getFileInDataFolder(filename: String, folderName: String? = null) : File


    @Throws(Exception::class)
    fun readFromTextFile(file: File): String?

    @Throws(Exception::class)
    fun readFromBinaryFile(file: File): ByteArray?

    @Throws(FileNotFoundException::class)
    fun createFileInputStream(file: File): InputStream


    @Throws(Exception::class)
    fun writeToTextFile(fileContent: String, file: File)

    @Throws(Exception::class)
    fun writeToBinaryFile(fileContent: ByteArray, file: File)

    @Throws(FileNotFoundException::class)
    fun createFileOutputStream(file: File): OutputStream


    fun deleteFolderRecursively(path: File)

}
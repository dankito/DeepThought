package net.dankito.data_access.filesystem

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream


interface IFileStorageService {

    fun getFileInDataFolder(filename: String) : File


    @Throws(Exception::class)
    fun readFromTextFile(filename: String): String?

    @Throws(Exception::class)
    fun readFromBinaryFile(filename: String): ByteArray?

    @Throws(FileNotFoundException::class)
    fun createFileInputStream(filename: String): InputStream


    @Throws(Exception::class)
    fun writeToTextFile(fileContent: String, filename: String)

    @Throws(Exception::class)
    fun writeToBinaryFile(fileContent: ByteArray, filename: String)

    @Throws(FileNotFoundException::class)
    fun createFileOutputStream(filename: String): OutputStream


    fun deleteFolderRecursively(path: String)

}
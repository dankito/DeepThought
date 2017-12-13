package net.dankito.deepthought.ui.html

import net.dankito.deepthought.service.data.DataManager
import net.dankito.utils.OsHelper
import org.slf4j.LoggerFactory
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.concurrent.thread


class HtmlEditorExtractor(private val dataManager: DataManager, private val osHelper: OsHelper) {

    companion object {
        private val PathOfAnyResourceInDeepThoughtJavaFx = "java_html_editor.txt" // take the name of any Resource that's for sure there

        private val PathOfAnyResourceInDeepThoughtAndroidApp = "android_html_editor.txt" // take the name of any Resource that's for sure there

        private val log = LoggerFactory.getLogger(HtmlEditorExtractor::class.java)
    }


    var unzippedHtmlEditorFilePath: String? = null

    private var isHtmlEditorExtracted = false

    private val htmlEditorExtractedListeners = mutableSetOf<(String) -> Unit>()


    fun addHtmlEditorExtractedListener(listener: (String) -> Unit) {
        if(isHtmlEditorExtracted) {
            unzippedHtmlEditorFilePath?.let { callHtmlEditorExtractedListener(listener, it)}
        }
        else {
            htmlEditorExtractedListeners.add(listener)
        }
    }

    private fun htmlEditorExtracted(unzippedHtmlEditorFilePath: String) {
        isHtmlEditorExtracted = true

        htmlEditorExtractedListeners.forEach { callHtmlEditorExtractedListener(it, unzippedHtmlEditorFilePath) }

        htmlEditorExtractedListeners.clear()
    }

    private fun callHtmlEditorExtractedListener(listener: (String) -> Unit, unzippedHtmlEditorFilePath: String) {
        listener(unzippedHtmlEditorFilePath)
    }


    fun extractHtmlEditorIfNeededAsync() {
        thread { extractHtmlEditorIfNeeded() }
    }

    fun extractHtmlEditorIfNeeded(): String? {
        val htmlEditorDirectory = File(dataManager.dataFolderPath, HtmlEditorCommon.HtmlEditorFolderName)
//        JavaFileStorageService().deleteFolderRecursively(htmlEditorDirectory) // if CKEditor_start.html has been updated

        if (htmlEditorDirectory.exists() == false /*|| htmlEditorDirectory.*/) { // TODO: check if folder has correct size
            unzippedHtmlEditorFilePath = extractCKEditorToHtmlEditorFolder()
        }
        else {
            try {
                unzippedHtmlEditorFilePath = File(htmlEditorDirectory, HtmlEditorCommon.HtmlEditorFileName).toURI().toURL().toExternalForm()
            } catch (ex: Exception) {
                log.error("Could not build  from $htmlEditorDirectory and ${HtmlEditorCommon.HtmlEditorFileName}", ex)
            }
            // TODO: what to do in error case?
        }

        unzippedHtmlEditorFilePath?.let { htmlEditorExtracted(it) }

        return unzippedHtmlEditorFilePath
    }

    private fun extractCKEditorToHtmlEditorFolder(): String? {
        var htmlEditorPath: String? = null

        try {
            val htmlEditorDirectory = dataManager.dataFolderPath

            getJarFileForHtmlEditor()?.let { jar ->
                val enumEntries = jar.entries()

                while (enumEntries.hasMoreElements()) {
                    val entry = enumEntries.nextElement() as JarEntry
                    if(entry.isDirectory) {
                        continue
                    }

                    if(entry.name.startsWith(HtmlEditorCommon.HtmlEditorFolderName)) {
                        extractJarFileEntry(jar, entry, htmlEditorDirectory)

                        // curious behaviour here: on Windows HtmlEditorFolderAndFileName is separated by '\', but entry.name by '/' -> check startsWith(), endsWith() and length
                        if(entry.name.endsWith(HtmlEditorCommon.HtmlEditorFileName) && entry.name.length == HtmlEditorCommon.HtmlEditorFolderAndFileName.length) {
                            val file = File(htmlEditorDirectory, entry.name)
                            val url = file.toURI().toURL()
                            htmlEditorPath = url.toExternalForm()
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            log.error("Could not extract Html Editor from .jar file", ex)
        }

        return htmlEditorPath
    }


    private fun getJarFileForHtmlEditor(): JarFile? {
        if(osHelper.isRunningOnAndroid) {
            return getJarFileOfResource(PathOfAnyResourceInDeepThoughtAndroidApp)
        }
        else {
            return getJarFileOfResource(PathOfAnyResourceInDeepThoughtJavaFx)
        }
    }

    private fun getJarFileOfResource(pathOfResourceInJar: String): JarFile? {
        try {
            getJarFilePathOfResource(pathOfResourceInJar)?.let { jarPath ->
                return JarFile(jarPath)
            }
        } catch (e: Exception) {
            log.error("Could not retrieve Jar file path for resource $pathOfResourceInJar", e)
        }

        return null
    }

    private fun getJarFilePathOfResource(pathOfResourceInJar: String): String? {
        try {
            val url = HtmlEditorExtractor::class.java.getClassLoader().getResource(pathOfResourceInJar)

            var jarPathString = url!!.toExternalForm()
            jarPathString = jarPathString.replace("!/" + pathOfResourceInJar, "") // remove path of resource from path and last '!/' as well (file ends with '.jar!/'

            if (jarPathString.startsWith("jar:")) {// remove leading jar:
                jarPathString = jarPathString.substring(4)
            }
            if (jarPathString.startsWith("file:")) { // remove leading file:
                jarPathString = jarPathString.substring(5)
            }

            return jarPathString
        } catch (e: Exception) {
            log.error("Could not retrieve Jar file path of resource $pathOfResourceInJar", e)
        }

        return null
    }

    @Throws(IOException::class)
    private fun extractJarFileEntry(jar: JarFile, entry: JarEntry, destinationDir: File?): File? {
        var destinationFile = File(destinationDir, entry.name)

        if (destinationDir != null && destinationDir.path.isNullOrEmpty()) {
            destinationFile = File(entry.name)
        }

        try {
            destinationFile.parentFile.mkdirs()
            try {
                destinationFile.createNewFile()
            } catch (e: Exception) {
                log.error("Could not create file " + destinationFile.absolutePath, e)
            }

            val inputStream = jar.getInputStream(entry) // get the input stream
            writeToFile(inputStream, destinationFile)

            return destinationFile
        } catch (e: Exception) {
            log.error("Could not write Jar entry " + entry.name + " to temp file " + destinationFile, e)
        }

        return null
    }


    @Throws(Exception::class)
    private fun writeToFile(inputStream: InputStream, destinationFile: File) {
        // TODO: what to do if file already exists?
        ensureFileExists(destinationFile)

        var outputStream: OutputStream? = null

        try {
            outputStream = FileOutputStream(destinationFile)

            // TODO: use IFileStorageService
            inputStream.copyTo(outputStream, 16 * 1024)
//            outputStream?.
//            var read = 0
//            val bytes = ByteArray(1024)
//
//            while ((read = inputStream.read(bytes)) != -1) {
//                outputStream?.write(bytes, 0, read)
//            }
        } catch (ex: IOException) {
            log.error("Could not write InputStream to file " + destinationFile.absolutePath, ex)
            throw ex
        } finally {
            try {
                outputStream?.flush();
                outputStream?.close()

                inputStream.close()
            } catch (e: IOException) { }
        }
    }

    @Throws(IOException::class)
    private fun ensureFileExists(destinationFile: File) {
        if (destinationFile.parentFile != null && destinationFile.parentFile.exists() === false)
            destinationFile.parentFile.mkdirs()

        if (destinationFile.exists() === false)
            destinationFile.createNewFile()
    }

}
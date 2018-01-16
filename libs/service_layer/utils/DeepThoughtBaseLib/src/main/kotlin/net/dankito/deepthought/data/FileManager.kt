package net.dankito.deepthought.data

import net.dankito.deepthought.model.FileLink
import net.dankito.utils.IPlatformConfiguration
import java.io.File


class FileManager(private val platformConfiguration: IPlatformConfiguration) {

    fun createLocalFile(localFile: File): FileLink {
        val relativePath = localFile.toRelativeString(platformConfiguration.getApplicationFolder())
        val file = FileLink(relativePath, localFile.name)

        return file
    }

    fun getLocalPathForFile(file: FileLink): File {
        // TODO: read from LocalFileInfo
        return File(platformConfiguration.getApplicationFolder(), file.uriString)
    }

}
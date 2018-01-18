package net.dankito.service.search

import com.nhaarman.mockito_kotlin.mock
import net.dankito.deepthought.files.FileManager
import net.dankito.utils.services.hashing.HashService
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File


class SearchFilesIntegrationTest : LuceneSearchEngineIntegrationTestBase() {


    private val fileManager = FileManager(underTest, localFileInfoService, mock(), platformConfiguration, HashService(), eventBus)


    @Test
    fun persistFile_LocalFileInfoGetsFound() {
        val tempFile = createTempFile()
        val file = fileManager.createLocalFile(tempFile)

        assertThat(file.localFileInfo, notNullValue())
        assertThat(file.id, nullValue())


        fileService.persist(file)

        assertThat(file.id, notNullValue())
        assertThat(file.localFileInfo?.id, notNullValue())

        try { Thread.sleep(1000) } catch(ignored: Exception) { } // file gets indexed async -> wait some time


        file.localFileInfo = null

        val localFileInfo = underTest.getLocalFileInfo(file)

        assertThat(localFileInfo, notNullValue())
        assertThat(localFileInfo?.file, notNullValue())

        assertThat(localFileInfo?.path, `is`(tempFile.absolutePath))
        assertThat(file.fileSize, `is`(localFileInfo?.fileSize))
        assertThat(file.fileLastModified, `is`(localFileInfo?.fileLastModified))
        assertThat(file.hashSHA512, `is`(localFileInfo?.hashSHA512))
    }

    @Test
    fun deletePersistedFile_LocalFileInfoGetsAlsoDeleted() {
        val tempFile = createTempFile()
        val file = fileManager.createLocalFile(tempFile)
        fileService.persist(file)

        try { Thread.sleep(1000) } catch(ignored: Exception) { } // file gets indexed async -> wait some time

        val localFileInfo = underTest.getLocalFileInfo(file)

        assertThat(localFileInfo, notNullValue())
        assertThat(file.id, notNullValue())
        assertThat(file.localFileInfo?.id, notNullValue())

        val fileCopy = fileManager.createLocalFile(tempFile)
        fileCopy.id = file.id


        fileService.delete(file)


        assertThat(file.deleted, `is`(true))
        assertThat(localFileInfo?.deleted, `is`(true))

        val isLocalFileInfoStillInIndex = underTest.getLocalFileInfo(fileCopy)

        assertThat(isLocalFileInfoStillInIndex, nullValue())
    }


    private fun createTempFile(): File {
        val tempFile = File.createTempFile("SearchEngineTest", "tmp")

        tempFile.deleteOnExit()

        return tempFile
    }

}
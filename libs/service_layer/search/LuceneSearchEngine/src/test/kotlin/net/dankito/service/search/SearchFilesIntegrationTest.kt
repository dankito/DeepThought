package net.dankito.service.search

import com.nhaarman.mockito_kotlin.mock
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.model.FileLink
import net.dankito.service.search.specific.FilesSearch
import net.dankito.utils.ThreadPool
import net.dankito.utils.services.hashing.HashService
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class SearchFilesIntegrationTest : LuceneSearchEngineIntegrationTestBase() {

    companion object {
        private const val File1Name = "File1"
        private const val File1Uri = "C:\\textfile1.txt"
        private const val File1UriUniquePart = "Textfile1"
        private const val File1IsLocalFile = true
        private const val File1Description = "An amazing text file"
        private const val File1DescriptionUniquePart = "Amazing"
        private const val File1SourceUri = "https://www.example.com/textfile.txt"
        private const val File1SourceUriUniquePart = "exampLe"
    }


    private val fileManager = FileManager(underTest, localFileInfoService, mock(), platformConfiguration, HashService(), eventBus, ThreadPool())


    @Test
    fun persistFile_FileGetFoundByUri() {
        testField(File1UriUniquePart, searchUri = true)
    }

    @Test
    fun persistFile_FileGetFoundByName() {
        testField(File1Name, searchName = true)
    }

    @Test
    fun persistFile_FileGetFoundByDescription() {
        testField(File1DescriptionUniquePart, searchDescription = true)
    }

    @Test
    fun persistFile_FileGetFoundBySourceUri() {
        testField(File1SourceUriUniquePart, searchSourceUri = true)
    }

    private fun testField(fieldValueToSearchFor: String, searchUri: Boolean = false, searchName: Boolean = false,
                          searchDescription: Boolean = false, searchSourceUri: Boolean = false) {
        var resultTested = false
        val waitForResultLatch = CountDownLatch(1)

        val file = persistTestFiles(File1IsLocalFile)


        underTest.searchFiles(FilesSearch(fieldValueToSearchFor, searchUri = searchUri, searchName = searchName, searchDescription = searchDescription,
                searchSourceUri = searchSourceUri) { result ->
            assertThat(result.size, `is`(1))
            assertThat(result[0], `is`(file))

            resultTested = true
            waitForResultLatch.countDown()
        })


        try { waitForResultLatch.await(4, TimeUnit.SECONDS) } catch(ignored: Exception) { }

        assertThat(resultTested, `is`(true))
    }


    @Test
    fun persistLocalFile_FileGetFoundByIsLocalFile() {
        testIsLocalFileField("", true, 3, false)
    }

    @Test
    fun persistRemoteFile_FileGetFoundByIsNotLocalFile() {
        testIsLocalFileField("", false, 3, true)
    }

    @Test
    fun persistLocalFilesOnly_FileGetFoundByIsLocalFileAndName() {
        testIsLocalFileField(File1Name, true, 3, true, true)
    }

    @Test
    fun persistRemoteFilesOnly_FileGetFoundByIsNotLocalFileAndName() {
        testIsLocalFileField(File1Name, false, 3, false, true)
    }

    @Test
    fun persistLocalFilesOnly_AllFilesGetFound() {
        testIsLocalFileFieldOfSameType(true)
    }

    @Test
    fun persistRemoteFilesOnly_AllFilesGetFound() {
        testIsLocalFileFieldOfSameType(false)
    }

    private fun testIsLocalFileField(searchTerm: String, isTestFileLocalFile: Boolean, countDummyFiles: Int = 3, isDummyLocalFile: Boolean, searchName: Boolean = false) {
        var resultTested = false
        val waitForResultLatch = CountDownLatch(1)

        val file = persistTestFiles(isTestFileLocalFile, countDummyFiles, isDummyLocalFile)

        val fileType = if(isTestFileLocalFile == false) FilesSearch.FileType.RemoteFilesOnly else FilesSearch.FileType.LocalFilesOnly


        underTest.searchFiles(FilesSearch(searchTerm, fileType, false, searchName, false, false) { result ->
            assertThat(result.size, `is`(1))
            assertThat(result[0], `is`(file))

            resultTested = true
            waitForResultLatch.countDown()
        })


        try { waitForResultLatch.await(4, TimeUnit.SECONDS) } catch (ignored: Exception) { }

        assertThat(resultTested, `is`(true))
    }

    private fun testIsLocalFileFieldOfSameType(areFilesLocal: Boolean, countDummyFiles: Int = 3) {
        var resultTested = false
        val waitForResultLatch = CountDownLatch(1)

        val file = persistTestFiles(areFilesLocal, countDummyFiles, areFilesLocal)

        val fileType = if(areFilesLocal == false) FilesSearch.FileType.RemoteFilesOnly else FilesSearch.FileType.LocalFilesOnly


        underTest.searchFiles(FilesSearch("", fileType) { result ->
            assertThat(result.size, `is`(1 + countDummyFiles))
            assertThat(result.contains(file), `is`(true))

            resultTested = true
            waitForResultLatch.countDown()
        })


        try { waitForResultLatch.await(4, TimeUnit.SECONDS) } catch (ignored: Exception) { }

        assertThat(resultTested, `is`(true))
    }


    @Test
    fun persistFile_LocalFileInfoGetsFound() {
        val tempFile = createTempFile()
        val file = fileManager.createLocalFile(tempFile)

        assertThat(file.localFileInfo, notNullValue())
        assertThat(file.id, nullValue())


        fileService.persist(file)

        assertThat(file.id, notNullValue())
        assertThat(file.localFileInfo?.id, notNullValue())

        waitTillFileGetsIndexed() // file gets indexed async and hash calculated async -> wait some time


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

        waitTillFileGetsIndexed() // file gets indexed async -> wait some time

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


    private fun persistTestFiles(isTestFileLocalFile: Boolean = true, countDummyFiles: Int = 3, isDummyLocalFile: Boolean = true): FileLink {
        val file = FileLink(File1Uri, File1Name, isTestFileLocalFile)
        file.description = File1Description
        file.sourceUriString = File1SourceUri

        fileService.persist(file)

        persistDummyFiles(countDummyFiles, isDummyLocalFile) // to create some noise

        waitTillFileGetsIndexed() // file gets indexed async -> wait some time
        return file
    }

    private fun persistDummyFiles(countDummyFiles: Int, isLocalFile: Boolean = true) {
        for(i in 0 until countDummyFiles) {
            val dummyFile = FileLink("/Dummy$i", "Dummy$i", isLocalFile)
            fileService.persist(dummyFile)
        }
    }

    private fun createTempFile(): File {
        val tempFile = File.createTempFile("SearchEngineTest", "tmp")

        tempFile.deleteOnExit()

        return tempFile
    }


    private fun waitTillFileGetsIndexed() {
        try {
            Thread.sleep(1000)
        } catch (ignored: Exception) { }
    }

}
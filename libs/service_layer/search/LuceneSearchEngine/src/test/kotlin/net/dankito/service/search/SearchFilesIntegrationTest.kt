package net.dankito.service.search

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.enums.FileTypeDefaultFolderName
import net.dankito.service.search.specific.FilesSearch
import net.dankito.service.search.specific.LocalFileInfoSearch
import net.dankito.service.search.writerandsearcher.FileLinkIndexWriterAndSearcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


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


    @Test
    fun persistFile_FileGetFoundByUri() {
        persistTestFilesAndTestField(File1UriUniquePart, searchUri = true)
    }

    @Test
    fun persistFile_FileGetFoundByName() {
        persistTestFilesAndTestField(File1Name, searchName = true)
    }

    @Test
    fun persistFile_FileGetFoundByDescription() {
        persistTestFilesAndTestField(File1DescriptionUniquePart, searchDescription = true)
    }

    @Test
    fun persistFile_FileGetFoundBySourceUri() {
        persistTestFilesAndTestField(File1SourceUriUniquePart, searchSourceUri = true)
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


        try { waitForResultLatch.await(4, TimeUnit.MINUTES) } catch (ignored: Exception) { }

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
    fun findAllFiles() {
        getAndTestFilesWithLocalFileInfo(10)
    }

    @Test
    fun findOnlyLocalFiles() {
        getAndTestFilesWithLocalFileInfo(5, FilesSearch.FileType.LocalFilesOnly)
    }

    private fun getAndTestFilesWithLocalFileInfo(countSearchResults: Int = 5, fileType: FilesSearch.FileType = FilesSearch.FileType.LocalOrRemoteFiles) {
        val fileIndexWriterAndSearcherField = underTest.javaClass.kotlin.declaredMemberProperties.filter { it.name == "fileIndexWriterAndSearcher" }.firstOrNull()
        fileIndexWriterAndSearcherField?.isAccessible = true
        val fileIndexWriterAndSearcher = fileIndexWriterAndSearcherField?.get(underTest) as? FileLinkIndexWriterAndSearcher

        for(i in 0 until 5) {
            fileService.persist(fileManager.createLocalFile(File("/tmp", "With_$i")))

            fileService.persist(FileLink("", "Remote_$i", false))
        }

        waitTillEntityGetsIndexed()


        testFilesWithLocalFileInfo(countSearchResults, fileType)
    }

    private fun testFilesWithLocalFileInfo(countSearchResults: Int, fileType: FilesSearch.FileType) {
        var resultTested = false
        val waitForResultLatch = CountDownLatch(1)


        underTest.searchFiles(FilesSearch("", fileType) { result ->
            assertThat(result.size, `is`(countSearchResults))

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
        assertThat(file.id, nullValue())

        val localFileInfo = fileManager.getStoredLocalFileInfo(file)
        assertThat(localFileInfo, notNullValue())
        assertThat(localFileInfo?.id, nullValue())


        filePersister.saveFile(file)

        assertThat(file.id, notNullValue())
        assertThat(localFileInfo?.id, notNullValue())

        waitTillEntityGetsIndexed() // file gets indexed async and hash calculated async -> wait some time


        underTest.searchLocalFileInfo(LocalFileInfoSearch(file.id) { result ->
            assertThat(result.isNotEmpty(), `is`(true))

            val retrievedLocalFileInfo = result[0]

            assertThat(retrievedLocalFileInfo, notNullValue())
            assertThat(retrievedLocalFileInfo.file, notNullValue())

            assertThat(retrievedLocalFileInfo.path, `is`(tempFile.absolutePath))
            assertThat(file.fileSize, `is`(retrievedLocalFileInfo.fileSize))
            assertThat(file.fileLastModified, `is`(retrievedLocalFileInfo.fileLastModified))
            assertThat(file.hashSHA512, `is`(retrievedLocalFileInfo.hashSHA512))
        })
    }

    @Test
    fun deletePersistedFile_LocalFileInfoGetsAlsoDeleted() {
        val tempFile = createTempFile()
        val file = fileManager.createLocalFile(tempFile)
        filePersister.saveFile(file)

        waitTillEntityGetsIndexed() // file gets indexed async -> wait some time

        underTest.searchLocalFileInfo(LocalFileInfoSearch(file.id) { result ->
            assertThat(result.isNotEmpty(), `is`(true))

            val retrievedLocalFileInfo = result[0]

            assertThat(retrievedLocalFileInfo, notNullValue())
            assertThat(file.id, notNullValue())
            assertThat(retrievedLocalFileInfo.id, notNullValue())

            val fileCopy = fileManager.createLocalFile(tempFile)
            fileCopy.id = file.id


            deleteEntityService.deleteFile(file)


            assertThat(file.deleted, `is`(true))
            assertThat(retrievedLocalFileInfo.deleted, `is`(true))


            underTest.searchLocalFileInfo(LocalFileInfoSearch(fileCopy.id) { isLocalFileInfoStillInIndex ->
                assertThat(isLocalFileInfoStillInIndex.isEmpty(), `is`(true))
            })
        })
    }


    @Test
    fun persistPdfFile_FileGetFoundByMimeType() {
        persistFileAndTestField(File("/tmp/book.pdf"), "pdf", searchMimeType = true)
    }

    @Test
    fun persistPngFile_FileGetFoundByMimeType() {
        persistFileAndTestField(File("/tmp/mock_up.png"), "png", searchMimeType = true)
    }

    @Test
    fun persistMp3File_FileGetFoundByMimeType() {
        persistFileAndTestField(File("/tmp/great_song.mp3"), "audio/mpeg", searchMimeType = true)
    }

    @Test
    fun persistMpegFile_FileGetFoundByMimeType() {
        persistFileAndTestField(File("/tmp/amazing_movie.mpeg"), "mpeg", searchMimeType = true)
    }


    @Test
    fun persistPdfFile_FileGetFoundByFileType() {
        persistFileAndTestField(File("/tmp/book.pdf"), FileTypeDefaultFolderName.Documents.folderName, searchFileType = true)
    }

    @Test
    fun persistPngFile_FileGetFoundByFileType() {
        persistFileAndTestField(File("/tmp/mock_up.png"), FileTypeDefaultFolderName.Images.folderName, searchFileType = true)
    }

    @Test
    fun persistMp3File_FileGetFoundByFileType() {
        persistFileAndTestField(File("/tmp/great_song.mp3"), FileTypeDefaultFolderName.Audio.folderName, searchFileType = true)
    }

    @Test
    fun persistMpegFile_FileGetFoundByFileType() {
        persistFileAndTestField(File("/tmp/amazing_movie.mpeg"), FileTypeDefaultFolderName.Video.folderName, searchFileType = true)
    }

    @Test
    fun persistExeFile_FileGetFoundByFileType() {
        persistFileAndTestField(File("/tmp/DeepThought.exe"), FileTypeDefaultFolderName.OtherFilesFolderName.folderName, searchFileType = true)
    }

    @Test
    fun persistApkFile_FileGetFoundByFileType() { // a file type with unknown Mime type
        persistFileAndTestField(File("/tmp/DeepThought.apk"), FileTypeDefaultFolderName.OtherFilesFolderName.folderName, searchFileType = true)
    }



    private fun persistFileAndTestField(localFile: File, fieldValueToSearchFor: String, searchUri: Boolean = false, searchName: Boolean = false,
                                 searchMimeType: Boolean = false, searchFileType: Boolean = false, searchDescription: Boolean = false, searchSourceUri: Boolean = false) {
        val file = fileManager.createLocalFile(localFile)
        filePersister.saveFile(file)

        testField(file, fieldValueToSearchFor, searchUri, searchName, searchMimeType, searchFileType, searchDescription, searchSourceUri)
    }

    private fun persistTestFilesAndTestField(fieldValueToSearchFor: String, searchUri: Boolean = false, searchName: Boolean = false,
                                     searchMimeType: Boolean = false, searchFileType: Boolean = false, searchDescription: Boolean = false, searchSourceUri: Boolean = false) {
        val file = persistTestFiles(File1IsLocalFile)

        testField(file, fieldValueToSearchFor, searchUri, searchName, searchMimeType, searchFileType, searchDescription, searchSourceUri)
    }

    private fun testField(file: FileLink, fieldValueToSearchFor: String, searchUri: Boolean, searchName: Boolean,
                          searchMimeType: Boolean = false, searchFileType: Boolean = false, searchDescription: Boolean, searchSourceUri: Boolean) {
        var resultTested = false
        val waitForResultLatch = CountDownLatch(1)


        underTest.searchFiles(FilesSearch(fieldValueToSearchFor, searchUri = searchUri, searchName = searchName, searchDescription = searchDescription,
                searchSourceUri = searchSourceUri) { result ->
            assertThat(result.size, `is`(1))
            assertThat(result[0], `is`(file))

            resultTested = true
            waitForResultLatch.countDown()
        })


        try {
            waitForResultLatch.await(4, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

        assertThat(resultTested, `is`(true))
    }


    private fun persistTestFiles(isLocalFile: Boolean = true, countDummyFiles: Int = 3, isDummyLocalFile: Boolean = true): FileLink {
        val file = FileLink(File1Uri, File1Name, isLocalFile)
        file.description = File1Description
        file.sourceUriString = File1SourceUri

        fileService.persist(file)

        persistDummyFiles(countDummyFiles, isDummyLocalFile) // to create some noise

        waitTillEntityGetsIndexed() // file gets indexed async -> wait some time
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

}
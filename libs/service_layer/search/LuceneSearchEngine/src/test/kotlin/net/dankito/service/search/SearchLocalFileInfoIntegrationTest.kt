package net.dankito.service.search

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.deepthought.model.enums.FileSyncStatus
import net.dankito.service.search.specific.LocalFileInfoSearch
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class SearchLocalFileInfoIntegrationTest : LuceneSearchEngineIntegrationTestBase() {


    @Test
    fun getOnlyLocalFileInfoWithStatusUpToDate() {
        getAndTestResult(5, FileSyncStatus.UpToDate)
    }

    @Test
    fun getOnlyLocalFileInfoWithStatusNotSynchronizedYet() {
        getAndTestResult(5, FileSyncStatus.NotSynchronizedYet)
    }


    @Test
    fun getOnlyLocalFileInfoNotHavingStatusUpToDate() {
        getAndTestResult(5, null, FileSyncStatus.UpToDate)
    }

    @Test
    fun getOnlyLocalFileInfoNotHavingStatusNotSynchronizedYet() {
        getAndTestResult(5, null, FileSyncStatus.NotSynchronizedYet)
    }


    private fun getAndTestResult(countSearchResults: Int = 5, hasSyncStatus: FileSyncStatus? = null, doesNotHaveSyncStatus: FileSyncStatus? = null) {
        createCountLocalFileInfoForEachSyncStatusType(countSearchResults)

        val resultHolder = search(hasSyncStatus, doesNotHaveSyncStatus)

        testResult(resultHolder, countSearchResults, hasSyncStatus, doesNotHaveSyncStatus)
    }

    private fun search(hasSyncStatus: FileSyncStatus?, doesNotHaveSyncStatus: FileSyncStatus?): AtomicReference<List<LocalFileInfo>?> {
        val resultHolder = AtomicReference<List<LocalFileInfo>?>(null)
        val waitForResultLatch = CountDownLatch(1)

        underTest.searchLocalFileInfo(LocalFileInfoSearch(null, hasSyncStatus, doesNotHaveSyncStatus) { result ->
            resultHolder.set(result)

            waitForResultLatch.countDown()
        })

        try { waitForResultLatch.await(4, TimeUnit.SECONDS) } catch (ignored: Exception) { }

        return resultHolder
    }

    private fun testResult(resultHolder: AtomicReference<List<LocalFileInfo>?>, countSearchResults: Int, hasSyncStatus: FileSyncStatus?, doesNotHaveSyncStatus: FileSyncStatus?) {
        assertThat(resultHolder.get(), notNullValue())
        assertThat(resultHolder.get()?.size, `is`(countSearchResults))

        resultHolder.get()?.forEach { localFileInfo ->
            hasSyncStatus?.let {
                assertThat(it, `is`(localFileInfo.syncStatus))
            }

            doesNotHaveSyncStatus?.let {
                assertThat(it, `is`(not(localFileInfo.syncStatus)))
            }
        }
    }


    private fun createCountLocalFileInfoForEachSyncStatusType(countLocalFileInfo: Int) {
        FileSyncStatus.values().forEach {
            createCountLocalFileInfoWithSyncStatus(countLocalFileInfo, it)
        }
    }

    private fun createCountLocalFileInfoWithSyncStatus(countLocalFileInfo: Int, syncStatus: FileSyncStatus) {
        for(i in 0 until countLocalFileInfo) {
            val file = FileLink("")
            fileService.persist(file)

            val localFileInfo = LocalFileInfo(file, syncStatus = syncStatus)
            localFileInfoService.persist(localFileInfo)
        }

        waitTillEntityGetsIndexed()
    }

}
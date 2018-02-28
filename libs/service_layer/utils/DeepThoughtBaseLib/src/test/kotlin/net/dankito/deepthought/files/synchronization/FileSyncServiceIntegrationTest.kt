package net.dankito.deepthought.files.synchronization

import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.model.LocalFileInfo
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.number.OrderingComparison
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class FileSyncServiceIntegrationTest : FileSyncServiceIntegrationTestBase() {


    @Test
    fun start_AssertFileServerGetsOpened() {
        connectDevices()

        Assert.assertThat(localNetworkSettings.fileSynchronizationPort, OrderingComparison.greaterThan(1023))
        Assert.assertThat(localConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, CoreMatchers.`is`(1))

        Assert.assertThat(remoteNetworkSettings.fileSynchronizationPort, OrderingComparison.greaterThan(1023))
        Assert.assertThat(remoteConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, CoreMatchers.`is`(1))
    }


    @Test
    fun persistFile_FileGetsSynchronized() {
        connectDevices()

        val waitLatch = CountDownLatch(2)
        waitTillEntityOfTypeIsSynchronized(remoteEventBus, FileLink::class.java, waitLatch)
        waitTillEntityOfTypeChanged(remoteEventBus, LocalFileInfo::class.java, waitLatch, null, EntityChangeType.Updated)


        val file = createFile()
        val fileId = file.id!!
        val localFileInfo = localFileManager.getStoredLocalFileInfo(file)

        waitLatch.await(SynchronizeEntityTimeoutInSeconds, TimeUnit.SECONDS)


        val synchronizedFile = remoteEntityManager.getEntityById(DeepThoughtFileLink::class.java, fileId)
        assertThat(synchronizedFile, notNullValue())

        val synchronizedLocalFileInfo = remoteFileManager.getStoredLocalFileInfo(synchronizedFile!!)
        assertThat(synchronizedLocalFileInfo, notNullValue())
        assertThat(synchronizedLocalFileInfo?.path, notNullValue())

        assertThat(File(synchronizedLocalFileInfo?.path!!).exists(), `is`(true))
        assertThat(synchronizedLocalFileInfo.fileSize, `is`(localFileInfo?.fileSize))
        assertThat(synchronizedLocalFileInfo.fileLastModified, `is`(localFileInfo?.fileLastModified))
        assertThat(synchronizedLocalFileInfo.hashSHA256, `is`(localFileInfo?.hashSHA256))
    }


}
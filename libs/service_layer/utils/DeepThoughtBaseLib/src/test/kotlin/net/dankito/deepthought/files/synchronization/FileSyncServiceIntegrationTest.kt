package net.dankito.deepthought.files.synchronization

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.LocalFileInfo
import net.dankito.service.data.messages.EntityChangeType
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
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

        assertThat(remoteEntityManager.getEntityById(FileLink::class.java, fileId), nullValue())

        waitLatch.await(SynchronizeEntityTimeoutInSeconds, TimeUnit.SECONDS)


        val synchronizedFile = remoteEntityManager.getEntityById(FileLink::class.java, fileId)
        assertThat(synchronizedFile, notNullValue())
        assertThat(synchronizedFile?.localFileInfo, notNullValue())
        assertThat(synchronizedFile?.localFileInfo?.path, notNullValue())

        assertThat(File(synchronizedFile?.localFileInfo?.path!!).exists(), `is`(true))
        assertThat(synchronizedFile.localFileInfo?.fileSize, `is`(file.localFileInfo?.fileSize))
        assertThat(synchronizedFile.localFileInfo?.fileLastModified, `is`(file.localFileInfo?.fileLastModified))
        assertThat(synchronizedFile.localFileInfo?.hashSHA512, `is`(file.localFileInfo?.hashSHA512))
    }


}
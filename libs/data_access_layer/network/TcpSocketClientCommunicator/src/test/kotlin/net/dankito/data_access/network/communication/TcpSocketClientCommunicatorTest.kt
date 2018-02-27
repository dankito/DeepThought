package net.dankito.data_access.network.communication

import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.data_access.network.communication.message.Response
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.enums.OsType
import net.dankito.util.ThreadPool
import net.dankito.util.hashing.HashService
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.serialization.JacksonJsonSerializer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


class TcpSocketClientCommunicatorTest {


    private lateinit var underTest: IClientCommunicator


    private lateinit var remoteDevice: Device

    private lateinit var discoveredRemoteDevice: DiscoveredDevice

    private lateinit var destinationAddress: SocketAddress


    @Before
    @Throws(Exception::class)
    fun setUp() {
        setUpRemoteDevice()

        val networkSettings = NetworkSettings(remoteDevice, User("Local", UUID.randomUUID().toString()))

        underTest = TcpSocketClientCommunicator(networkSettings, Mockito.mock(IDeviceRegistrationHandler::class.java), Mockito.mock(IEntityManager::class.java),
                JacksonJsonSerializer(), Mockito.mock(IBase64Service::class.java), HashService(), ThreadPool())

        val countDownLatch = CountDownLatch(1)

        underTest.start(MESSAGES_RECEIVER_PORT) { _, messagesReceiverPort, _ ->
            discoveredRemoteDevice.messagesPort = messagesReceiverPort
            destinationAddress = InetSocketAddress("localhost", messagesReceiverPort)
            countDownLatch.countDown()
        }

        try {
            countDownLatch.await(1, TimeUnit.SECONDS)
        } catch (ignored: Exception) {
        }

    }

    protected fun setUpRemoteDevice() {
        remoteDevice = Device(DEVICE_NAME, DEVICE_UNIQUE_ID, DEVICE_OS_TYPE, DEVICE_OS_NAME, DEVICE_OS_VERSION, "")
        val idField = BaseEntity::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(remoteDevice, DEVICE_ID)

        discoveredRemoteDevice = DiscoveredDevice(remoteDevice, "localhost")
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {

    }

    @Test
    @Throws(Exception::class)
    fun getDeviceInfo() {
        val responseHolder = AtomicReference<Response<DeviceInfo>>()
        val countDownLatch = CountDownLatch(1)

        underTest.getDeviceInfo(destinationAddress) { response ->
            responseHolder.set(response)
            countDownLatch.countDown()
        }


        try {
            countDownLatch.await(1, TimeUnit.MINUTES)
        } catch (ignored: Exception) {
        }

        assertThat(responseHolder.get(), notNullValue())

        val response = responseHolder.get()
        assertThat(response.isCouldHandleMessage, `is`(true))

        val remoteDeviceInfo = response.body
        assertThat<DeviceInfo>(remoteDeviceInfo, notNullValue())

        remoteDeviceInfo?.let { remoteDeviceInfo ->
            assertThat(remoteDeviceInfo.id, `is`(DEVICE_ID))
            assertThat(remoteDeviceInfo.uniqueDeviceId, `is`(DEVICE_UNIQUE_ID))
        }
    }

    companion object {

        protected val MESSAGES_RECEIVER_PORT = 54321

        protected val DEVICE_ID = "1"

        protected val DEVICE_UNIQUE_ID = "Remote_1"

        protected val DEVICE_NAME = "Love"

        protected val DEVICE_OS_NAME = "Arch Linux"

        protected val DEVICE_OS_VERSION = "4.9"

        protected val DEVICE_OS_TYPE = OsType.DESKTOP
    }

}
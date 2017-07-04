package net.dankito.deepthought.communication

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.TcpSocketClientCommunicator
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.*
import net.dankito.data_access.network.discovery.UdpDevicesDiscoverer
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.model.NetworkSettings
import net.dankito.deepthought.model.enums.OsType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.service.synchronization.*
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.ThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.services.hashing.IBase64Service
import org.hamcrest.CoreMatchers.*
import org.hamcrest.number.OrderingComparison.greaterThan
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class CommunicationManagerTest {

    companion object {
        const val LocalDeviceName = "Local"
        val LocalOsType = OsType.ANDROID

        const val RemoteDeviceName = "Remote"
        val RemoteOsType = OsType.DESKTOP

        const val InitializationTimeoutInSeconds = 500L
        const val FindRemoteDeviceTimeoutInSeconds = 300L
    }


    private val threadPool = ThreadPool()

    private val localization: Localization = mock()

    private val base64Service: IBase64Service = mock()

    private val fileStorageService = JavaFileStorageService()


    private lateinit var localDevice: Device

    private val localPlatformConfiguration = object: IPlatformConfiguration {
        override fun getUserName(): String { return "Rieka" }

        override fun getDeviceName(): String? { return LocalDeviceName }

        override fun getOsType(): OsType { return LocalOsType }

        override fun getOsName(): String { return "Android" }

        override fun getOsVersion(): Int { return 6 }

        override fun getOsVersionString(): String { return "6.0" }

        override fun getDefaultDataFolder(): File { return File(File(File("data"), "test"), "test1") }
    }

    private lateinit var localNetworkSettings: INetworkSettings

    private lateinit var localRegistrationHandler: IDeviceRegistrationHandler

    private val localDevicesDiscoverer = UdpDevicesDiscoverer(threadPool)

    private lateinit var localEntityManager: IEntityManager

    private lateinit var localDataManager: DataManager

    private lateinit var localClientCommunicator: IClientCommunicator

    private lateinit var localSyncManager: CouchbaseLiteSyncManager

    private lateinit var localConnectedDevicesService: IConnectedDevicesService

    private lateinit var localCommunicationManager: ICommunicationManager


    // and the same for the remote device

    private lateinit var remoteDevice: Device

    private val remotePlatformConfiguration = object: IPlatformConfiguration {
        override fun getUserName(): String { return "dankito" }

        override fun getDeviceName(): String? { return RemoteDeviceName }

        override fun getOsType(): OsType { return RemoteOsType }

        override fun getOsName(): String { return "Arch Linux" }

        override fun getOsVersion(): Int { return 1 }

        override fun getOsVersionString(): String { return "0.1" }

        override fun getDefaultDataFolder(): File { return File(File(File("data"), "test"), "test2") }
    }

    private lateinit var remoteNetworkSettings: INetworkSettings

    private lateinit var remoteRegistrationHandler: IDeviceRegistrationHandler

    private val remoteDevicesDiscoverer = UdpDevicesDiscoverer(threadPool)

    private lateinit var remoteEntityManager: IEntityManager

    private lateinit var remoteDataManager: DataManager

    private lateinit var remoteClientCommunicator: IClientCommunicator

    private lateinit var remoteSyncManager: CouchbaseLiteSyncManager

    private lateinit var remoteConnectedDevicesService: IConnectedDevicesService

    private lateinit var remoteCommunicationManager: ICommunicationManager


    @Before
    @Throws(Exception::class)
    fun setUp() {
        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultDataFolder().path)
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultDataFolder().path)

        whenever(base64Service.encode(any<ByteArray>())).thenReturn("fake_base64_encoded_string")

        setupLocalDevice()

        setupRemoteDevice()
    }

    private fun setupLocalDevice() {
        localRegistrationHandler = mock()

        val entityManagerConfiguration = EntityManagerConfiguration(localPlatformConfiguration.getDefaultDataFolder().path, "test")
        localEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        localDataManager = DataManager(localEntityManager, entityManagerConfiguration, DefaultDataInitializer(localPlatformConfiguration, localization), localPlatformConfiguration)
        val initializationLatch = CountDownLatch(1)

        localDataManager.addInitializationListener {
            localDevice = localDataManager.localDevice
            localNetworkSettings = NetworkSettings(localDevice, localDataManager.localUser)

            localClientCommunicator = TcpSocketClientCommunicator(localNetworkSettings, localRegistrationHandler, base64Service, threadPool)

            localSyncManager = CouchbaseLiteSyncManager(localEntityManager as CouchbaseLiteEntityManagerBase, localNetworkSettings, threadPool)

            localConnectedDevicesService = ConnectedDevicesService(localDevicesDiscoverer, localClientCommunicator, localSyncManager, localNetworkSettings, localEntityManager)

            localCommunicationManager = CommunicationManager(localConnectedDevicesService, localSyncManager, localClientCommunicator, localRegistrationHandler, localNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)
    }


    private fun setupRemoteDevice() {
        remoteRegistrationHandler = mock<IDeviceRegistrationHandler>()

        val entityManagerConfiguration = EntityManagerConfiguration(remotePlatformConfiguration.getDefaultDataFolder().path, "test")
        remoteEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        remoteDataManager = DataManager(remoteEntityManager, entityManagerConfiguration, DefaultDataInitializer(remotePlatformConfiguration, localization), remotePlatformConfiguration)
        val initializationLatch = CountDownLatch(1)

        remoteDataManager.addInitializationListener {
            remoteDevice = remoteDataManager.localDevice
            remoteNetworkSettings = NetworkSettings(remoteDevice, remoteDataManager.localUser)

            remoteClientCommunicator = TcpSocketClientCommunicator(remoteNetworkSettings, remoteRegistrationHandler, base64Service, threadPool)

            remoteSyncManager = CouchbaseLiteSyncManager(remoteEntityManager as CouchbaseLiteEntityManagerBase, remoteNetworkSettings, threadPool)

            remoteConnectedDevicesService = ConnectedDevicesService(remoteDevicesDiscoverer, remoteClientCommunicator, remoteSyncManager, remoteNetworkSettings, remoteEntityManager)

            remoteCommunicationManager = CommunicationManager(remoteConnectedDevicesService, remoteSyncManager, remoteClientCommunicator, remoteRegistrationHandler, remoteNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)
    }


    @After
    @Throws(Exception::class)
    fun tearDown() {
        localCommunicationManager.stop()
        localEntityManager.close()

        remoteCommunicationManager.stop()
        remoteEntityManager.close()

        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultDataFolder().path)
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultDataFolder().path)
    }


    @Test
    fun otherDeviceGetsFound() {
        val localDiscoveredDevicesList: MutableList<DiscoveredDevice> = CopyOnWriteArrayList<DiscoveredDevice>()
        val remoteDiscoveredDevicesList = CopyOnWriteArrayList<DiscoveredDevice>()
        val countDownLatch = CountDownLatch(2)

        localConnectedDevicesService.addDiscoveredDevicesListener(createDiscoveredDevicesListener(localDiscoveredDevicesList, countDownLatch))

        remoteConnectedDevicesService.addDiscoveredDevicesListener(createDiscoveredDevicesListener(remoteDiscoveredDevicesList, countDownLatch))

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localDiscoveredDevicesList.size, `is`(1))
        assertThat(localDiscoveredDevicesList.get(0).device, `is`(remoteDevice))

        assertThat(remoteDiscoveredDevicesList.size, `is`(1))
        assertThat(remoteDiscoveredDevicesList.get(0).device, `is`(localDevice))
    }


    @Test
    fun localDeviceRequestsSynchronization_EnteredResponseIsCorrect_SynchronizationIsAllowed() {
        var result: RespondToSynchronizationPermittingChallengeResponseBody? = null
        var correctResponse: String = "not_valid"
        val countDownLatch = CountDownLatch(2)

        whenever(remoteRegistrationHandler.shouldPermitSynchronizingWithDevice(any(), any())).doAnswer { invocation ->
            val callback = invocation.arguments[1] as (DeviceInfo, Boolean) -> Unit
            callback(invocation.arguments[0] as DeviceInfo, true)
        }

        whenever(remoteRegistrationHandler.showResponseToEnterOnOtherDeviceNonBlocking(any(), any())).then { answer ->
            correctResponse = answer.getArgument<String>(1)
            null
        }

        whenever(remoteRegistrationHandler.deviceHasBeenPermittedToSynchronize(any<DiscoveredDevice>(), any<SyncInfo>())).thenReturn(mock<SyncInfo>())

        localConnectedDevicesService.addDiscoveredDevicesListener(informWhenRemoteDeviceDiscovered(countDownLatch) { discoveredDevice ->
            localClientCommunicator.requestPermitSynchronization(discoveredDevice) { permitResponse ->
                permitResponse.body?.let { body ->
                    localClientCommunicator.respondToSynchronizationPermittingChallenge(discoveredDevice, body.nonce!!, correctResponse, mock<SyncInfo>()) { challengeResponse ->
                        result = challengeResponse.body
                        countDownLatch.countDown()
                    }
                }

                permitResponse.error?.let { countDownLatch.countDown() }
            }
        })

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(result, notNullValue())

        result?.let { result ->
            assertThat(result.result, `is`(RespondToSynchronizationPermittingChallengeResult.ALLOWED))
            assertThat(result.syncInfo, notNullValue())
            assertThat(result.synchronizationPort, greaterThan(1023))
        }
    }


    @Test
    fun localDeviceRequestsSynchronization_RemoteDeniesSynchronization() {
        var responseBody: RequestPermitSynchronizationResponseBody? = null
        val countDownLatch = CountDownLatch(2)

        whenever(remoteRegistrationHandler.shouldPermitSynchronizingWithDevice(any(), any())).doAnswer { invocation ->
            val callback = invocation.arguments[1] as (DeviceInfo, Boolean) -> Unit
            callback(invocation.arguments[0] as DeviceInfo, false)
        }

        localConnectedDevicesService.addDiscoveredDevicesListener(informWhenRemoteDeviceDiscovered(countDownLatch) { discoveredDevice ->
            localClientCommunicator.requestPermitSynchronization(discoveredDevice) { permitResponse ->
                responseBody = permitResponse.body
                countDownLatch.countDown()
            }
        })

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(responseBody, notNullValue())

        responseBody?.let { result ->
            assertThat(result.result, `is`(RequestPermitSynchronizationResult.DENIED))
            assertThat(result.nonce, nullValue())
        }
    }


    private fun startCommunicationManagersAndWait(countDownLatch: CountDownLatch) {
        startCommunicationManagersAndWait(countDownLatch, FindRemoteDeviceTimeoutInSeconds)
    }

    private fun startCommunicationManagersAndWait(countDownLatch: CountDownLatch, timeoutInMillis: Long) {
        startCommunicationManagers()

        countDownLatch.await(timeoutInMillis, TimeUnit.SECONDS)
    }

    private fun startCommunicationManagers() {
        localCommunicationManager.startAsync()

        remoteCommunicationManager.startAsync()
    }


    private fun createDiscoveredDevicesListener(discoveredDevicesList: MutableList<DiscoveredDevice>, latchToCountDownOnDeviceDiscovered: CountDownLatch? = null,
                                                latchToCountDownOnDeviceDisconnected: CountDownLatch? = null): DiscoveredDevicesListener {
        return object : DiscoveredDevicesListener {
            override fun deviceDiscovered(connectedDevice: DiscoveredDevice, type: DiscoveredDeviceType) {
                discoveredDevicesList.add(connectedDevice)

                latchToCountDownOnDeviceDiscovered?.countDown()
            }

            override fun disconnectedFromDevice(disconnectedDevice: DiscoveredDevice) {
                discoveredDevicesList.remove(disconnectedDevice)

                latchToCountDownOnDeviceDisconnected?.countDown()
            }

        }
    }

    private fun informWhenRemoteDeviceDiscovered(latchToCountDownOnDeviceDiscovered: CountDownLatch? = null, callback: (DiscoveredDevice) -> Unit): DiscoveredDevicesListener {
        return object : DiscoveredDevicesListener {
            override fun deviceDiscovered(connectedDevice: DiscoveredDevice, type: DiscoveredDeviceType) {
                callback(connectedDevice)

                latchToCountDownOnDeviceDiscovered?.countDown()
            }

            override fun disconnectedFromDevice(disconnectedDevice: DiscoveredDevice) {
            }

        }
    }

}
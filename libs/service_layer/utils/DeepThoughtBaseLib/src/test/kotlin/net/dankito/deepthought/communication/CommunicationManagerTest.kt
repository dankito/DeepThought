package net.dankito.deepthought.communication

import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.TcpSocketClientCommunicator
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.data_access.network.discovery.UdpDevicesDiscoverer
import net.dankito.deepthought.model.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.service.synchronization.*
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.ThreadPool
import net.dankito.utils.services.hashing.IBase64Service
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
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

    private val base64Service: IBase64Service = Mockito.mock(IBase64Service::class.java)

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

    private lateinit var localPermissionHandler: IsSynchronizationPermittedHandler

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

    private lateinit var remotePermissionHandler: IsSynchronizationPermittedHandler

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

        setupLocalDevice()

        setupRemoteDevice()
    }

    private fun setupLocalDevice() {
        localPermissionHandler = Mockito.mock(IsSynchronizationPermittedHandler::class.java)

        val entityManagerConfiguration = EntityManagerConfiguration(localPlatformConfiguration.getDefaultDataFolder().path, "test")
        localEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        localDataManager = DataManager(localEntityManager, entityManagerConfiguration, DefaultDataInitializer(localPlatformConfiguration), localPlatformConfiguration)
        val initializationLatch = CountDownLatch(1)

        localDataManager.addInitializationListener {
            localDevice = localDataManager.localDevice
            localNetworkSettings = NetworkSettings(localDevice)

            localClientCommunicator = TcpSocketClientCommunicator(localNetworkSettings, localPermissionHandler, base64Service, threadPool)

            localSyncManager = CouchbaseLiteSyncManager(localEntityManager as CouchbaseLiteEntityManagerBase, localNetworkSettings, threadPool)

            localConnectedDevicesService = ConnectedDevicesService(localDevicesDiscoverer, localClientCommunicator, localSyncManager, localNetworkSettings, localEntityManager)

            localCommunicationManager = CommunicationManager(localConnectedDevicesService, localSyncManager, localClientCommunicator, localNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)
    }


    private fun setupRemoteDevice() {
        remotePermissionHandler = Mockito.mock(IsSynchronizationPermittedHandler::class.java)

        val entityManagerConfiguration = EntityManagerConfiguration(remotePlatformConfiguration.getDefaultDataFolder().path, "test")
        remoteEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        remoteDataManager = DataManager(remoteEntityManager, entityManagerConfiguration, DefaultDataInitializer(remotePlatformConfiguration), remotePlatformConfiguration)
        val initializationLatch = CountDownLatch(1)

        remoteDataManager.addInitializationListener {
            remoteDevice = remoteDataManager.localDevice
            remoteNetworkSettings = NetworkSettings(remoteDevice)

            remoteClientCommunicator = TcpSocketClientCommunicator(remoteNetworkSettings, remotePermissionHandler, base64Service, threadPool)

            remoteSyncManager = CouchbaseLiteSyncManager(remoteEntityManager as CouchbaseLiteEntityManagerBase, remoteNetworkSettings, threadPool)

            remoteConnectedDevicesService = ConnectedDevicesService(remoteDevicesDiscoverer, remoteClientCommunicator, remoteSyncManager, remoteNetworkSettings, remoteEntityManager)

            remoteCommunicationManager = CommunicationManager(remoteConnectedDevicesService, remoteSyncManager, remoteClientCommunicator, remoteNetworkSettings)

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
        localCommunicationManager.startAsync()

        remoteConnectedDevicesService.addDiscoveredDevicesListener(createDiscoveredDevicesListener(remoteDiscoveredDevicesList, countDownLatch))
        remoteCommunicationManager.startAsync()

        countDownLatch.await(FindRemoteDeviceTimeoutInSeconds, TimeUnit.SECONDS)

        assertThat(localDiscoveredDevicesList.size, `is`(1))
        assertThat(localDiscoveredDevicesList.get(0).device, `is`(remoteDevice))

        assertThat(remoteDiscoveredDevicesList.size, `is`(1))
        assertThat(remoteDiscoveredDevicesList.get(0).device, `is`(localDevice))
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

}
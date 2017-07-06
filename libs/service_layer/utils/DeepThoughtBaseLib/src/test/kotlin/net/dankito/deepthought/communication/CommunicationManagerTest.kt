package net.dankito.deepthought.communication

import com.nhaarman.mockito_kotlin.*
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.TcpSocketClientCommunicator
import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.data_access.network.discovery.UdpDevicesDiscoverer
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.enums.ExtensibleEnumeration
import net.dankito.deepthought.model.enums.OsType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.service.synchronization.*
import net.dankito.service.synchronization.changeshandler.SynchronizedChangesHandler
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.ThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.services.hashing.IBase64Service
import net.dankito.utils.ui.IDialogService
import org.hamcrest.CoreMatchers.*
import org.hamcrest.number.OrderingComparison.greaterThan
import org.hamcrest.number.OrderingComparison.lessThanOrEqualTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class CommunicationManagerTest {

    companion object {
        const val LocalDeviceName = "Local"
        val LocalOsType = OsType.ANDROID

        const val RemoteDeviceName = "Remote"
        val RemoteOsType = OsType.DESKTOP

        const val InitializationTimeoutInSeconds = 5L
        const val FindRemoteDeviceTimeoutInSeconds = 300L // it really takes a long time till Couchbase opens its listener port

        private val log = LoggerFactory.getLogger(CommunicationManagerTest::class.java)
    }


    private val localization: Localization = Localization()

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

    private val localThreadPool = ThreadPool()

    private lateinit var localNetworkSettings: INetworkSettings

    private lateinit var localDialogService: IDialogService

    private lateinit var localInitialSyncManager: InitialSyncManager

    private val localRegisterAtRemote = AtomicBoolean(false)
    private val localPermitRemoteToSynchronize = AtomicBoolean(false)
    private val localCorrectChallengeResponse = AtomicReference<String>()

    private lateinit var localRegistrationHandler: IDeviceRegistrationHandler

    private val localDevicesDiscoverer = UdpDevicesDiscoverer(localThreadPool)

    private lateinit var localEntityManager: IEntityManager

    private lateinit var localDataManager: DataManager

    private lateinit var localClientCommunicator: IClientCommunicator

    private val localEntityChangedNotifier = EntityChangedNotifier(MBassadorEventBus())

    private lateinit var localSynchronizedChangesHandler: SynchronizedChangesHandler

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

    private val remoteThreadPool = ThreadPool()

    private lateinit var remoteNetworkSettings: INetworkSettings

    private lateinit var remoteDialogService: IDialogService

    private lateinit var remoteInitialSyncManager: InitialSyncManager

    private val remoteRegisterAtRemote = AtomicBoolean(false)
    private val remotePermitRemoteToSynchronize = AtomicBoolean(false)
    private val remoteCorrectChallengeResponse = AtomicReference<String>()

    private lateinit var remoteRegistrationHandler: IDeviceRegistrationHandler

    private val remoteDevicesDiscoverer = UdpDevicesDiscoverer(remoteThreadPool)

    private lateinit var remoteEntityManager: IEntityManager

    private lateinit var remoteDataManager: DataManager

    private lateinit var remoteClientCommunicator: IClientCommunicator

    private val remoteEntityChangedNotifier = EntityChangedNotifier(MBassadorEventBus())

    private lateinit var remoteSynchronizedChangesHandler: SynchronizedChangesHandler

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
        val entityManagerConfiguration = EntityManagerConfiguration(localPlatformConfiguration.getDefaultDataFolder().path, "test")
        localEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        localDataManager = DataManager(localEntityManager, entityManagerConfiguration, DefaultDataInitializer(localPlatformConfiguration, localization), localPlatformConfiguration)

        localDialogService = mock<IDialogService>()
        localInitialSyncManager = InitialSyncManager(localEntityManager, localization)

        val initializationLatch = CountDownLatch(1)

        localDataManager.addInitializationListener {
            localDevice = localDataManager.localDevice
            localNetworkSettings = NetworkSettings(localDevice, localDataManager.localUser)

            localSynchronizedChangesHandler = SynchronizedChangesHandler(localEntityManager, localEntityChangedNotifier)

            localSyncManager = CouchbaseLiteSyncManager(localEntityManager as CouchbaseLiteEntityManagerBase, localSynchronizedChangesHandler, localNetworkSettings)

            localRegistrationHandler = createDeviceRegistrationHandler(localRegisterAtRemote, localPermitRemoteToSynchronize, localCorrectChallengeResponse, localDataManager,
                    localInitialSyncManager, localDialogService, localization)

            localClientCommunicator = TcpSocketClientCommunicator(localNetworkSettings, localRegistrationHandler, base64Service, localThreadPool)

            localConnectedDevicesService = ConnectedDevicesService(localDevicesDiscoverer, localClientCommunicator, localSyncManager, localRegistrationHandler, localNetworkSettings, localEntityManager)

            localCommunicationManager = CommunicationManager(localConnectedDevicesService, localSyncManager, localClientCommunicator, localNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)
    }


    private fun setupRemoteDevice() {
        val entityManagerConfiguration = EntityManagerConfiguration(remotePlatformConfiguration.getDefaultDataFolder().path, "test")
        remoteEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        remoteDataManager = DataManager(remoteEntityManager, entityManagerConfiguration, DefaultDataInitializer(remotePlatformConfiguration, localization), remotePlatformConfiguration)

        remoteDialogService = mock<IDialogService>()
        remoteInitialSyncManager = InitialSyncManager(remoteEntityManager, localization)

        val initializationLatch = CountDownLatch(1)

        remoteDataManager.addInitializationListener {
            remoteDevice = remoteDataManager.localDevice
            remoteNetworkSettings = NetworkSettings(remoteDevice, remoteDataManager.localUser)

            remoteSynchronizedChangesHandler = SynchronizedChangesHandler(remoteEntityManager, remoteEntityChangedNotifier)

            remoteSyncManager = CouchbaseLiteSyncManager(remoteEntityManager as CouchbaseLiteEntityManagerBase, remoteSynchronizedChangesHandler, remoteNetworkSettings)

            val registrationHandlerInstance = createDeviceRegistrationHandler(remoteRegisterAtRemote, remotePermitRemoteToSynchronize, remoteCorrectChallengeResponse, remoteDataManager,
                    remoteInitialSyncManager, remoteDialogService, localization)
            remoteRegistrationHandler = spy<IDeviceRegistrationHandler>(registrationHandlerInstance)

            remoteClientCommunicator = TcpSocketClientCommunicator(remoteNetworkSettings, remoteRegistrationHandler, base64Service, remoteThreadPool)

            remoteConnectedDevicesService = ConnectedDevicesService(remoteDevicesDiscoverer, remoteClientCommunicator, remoteSyncManager, remoteRegistrationHandler, remoteNetworkSettings, remoteEntityManager)

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

        remoteConnectedDevicesService.addDiscoveredDevicesListener(createDiscoveredDevicesListener(remoteDiscoveredDevicesList, countDownLatch))

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localDiscoveredDevicesList.size, `is`(1))
        assertThat(localDiscoveredDevicesList.get(0).device, `is`(remoteDevice))

        assertThat(remoteDiscoveredDevicesList.size, `is`(1))
        assertThat(remoteDiscoveredDevicesList.get(0).device, `is`(localDevice))
    }


    @Test
    fun localDeviceRequestsSynchronization_EnteredResponseIsCorrect_SynchronizationIsAllowed() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(localConnectedDevicesService, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(localConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, `is`(1))

        assertThat(remoteNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(remoteConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, `is`(1))
    }


    @Test
    fun localDeviceRequestsSynchronization_EnteredResponseIsWrong_SynchronizationIsAllowed() {
        val wrongResponse: String = "not_valid"

        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)
        var countAskForChallenge = 0

        whenever(localDialogService.askForTextInput(any<CharSequence>(), anyOrNull(), anyOrNull(), any())).thenAnswer { invocation ->
            val callback = invocation.arguments[3] as (Boolean, String?) -> Unit
            countAskForChallenge++

            if(countAskForChallenge == 1) { // at first call return a false response
                callback(true, wrongResponse)
            }
            else {
                callback(true, remoteCorrectChallengeResponse.get())
            }
        }

        waitTillKnownSynchronizedDeviceConnected(localConnectedDevicesService, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(localConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, `is`(1))

        assertThat(remoteNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(remoteConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, `is`(1))
    }


    @Test
    fun localDeviceRequestsSynchronization_RemoteDeniesSynchronization() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(false)

        val countDownLatch = CountDownLatch(1)

        localNetworkSettings.addListener(object : NetworkSettingsChangedListener {
            override fun settingsChanged(networkSettings: INetworkSettings, setting: NetworkSetting, newValue: Any, oldValue: Any?) {
                if(setting == NetworkSetting.REMOVED_DEVICES_ASKED_FOR_PERMITTING_SYNCHRONIZATION) {
                    println("Counting down ...")
                    countDownLatch.countDown()
                }
            }
        })

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localNetworkSettings.synchronizationPort, lessThanOrEqualTo(0))
        assertThat(localConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, `is`(0))

        assertThat(remoteNetworkSettings.synchronizationPort, lessThanOrEqualTo(0))
        assertThat(remoteConnectedDevicesService.knownSynchronizedDiscoveredDevices.size, `is`(0))
    }


    @Test
    fun localDeviceRequestsSynchronization_RemoteInfoIsUsedForInitialSynchronization() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(localConnectedDevicesService, countDownLatch)


        startCommunicationManagersAndWait(countDownLatch)

        val localUser = localDataManager.localUser
        val remoteUser = remoteDataManager.localUser

        assertThat(localUser.universallyUniqueId, `is`(remoteUser.universallyUniqueId))
        assertThat(localUser.userName, `is`(remoteUser.userName))
        assertThat(localUser.firstName, `is`(remoteUser.firstName))
        assertThat(localUser.lastName, `is`(remoteUser.lastName))


        val localDeepThought = localDataManager.deepThought
        val remoteDeepThought = remoteDataManager.deepThought

        assertThat(localUser.id, `is`(remoteUser.id))
        assertThat(localDeepThought.id, `is`(not(remoteDeepThought.id)))

        testExtensibleEnumeration(localDeepThought.applicationLanguages, remoteDeepThought.applicationLanguages)
        testExtensibleEnumeration(localDeepThought.fileTypes, remoteDeepThought.fileTypes)
        testExtensibleEnumeration(localDeepThought.noteTypes, remoteDeepThought.noteTypes)
    }


    @Test
    fun remoteDeviceRequestsSynchronization_LocalInfoIsUsedForInitialSynchronization() {
        remoteRegisterAtRemote.set(true)
        localPermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(remoteDialogService, localCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(remoteConnectedDevicesService, countDownLatch)


        startCommunicationManagersAndWait(countDownLatch)

        val localUser = localDataManager.localUser
        val remoteUser = remoteDataManager.localUser

        assertThat(localUser.universallyUniqueId, `is`(remoteUser.universallyUniqueId))
        assertThat(localUser.userName, `is`(remoteUser.userName))
        assertThat(localUser.firstName, `is`(remoteUser.firstName))
        assertThat(localUser.lastName, `is`(remoteUser.lastName))


        val localDeepThought = localDataManager.deepThought
        val remoteDeepThought = remoteDataManager.deepThought

        assertThat(localUser.id, `is`(remoteUser.id))
        assertThat(localDeepThought.id, `is`(not(remoteDeepThought.id)))

        testExtensibleEnumeration(localDeepThought.applicationLanguages, remoteDeepThought.applicationLanguages)
        testExtensibleEnumeration(localDeepThought.fileTypes, remoteDeepThought.fileTypes)
        testExtensibleEnumeration(localDeepThought.noteTypes, remoteDeepThought.noteTypes)
    }

    private fun testExtensibleEnumeration(localEnumerations: Collection<ExtensibleEnumeration>, remoteEnumerations: Collection<ExtensibleEnumeration>) {
        assertThat(localEnumerations.size, `is`(remoteEnumerations.size))

        for(localEnum in localEnumerations) {
            var foundRemoteEnum: ExtensibleEnumeration? = null

            for(remoteEnum in remoteEnumerations) {
                if(localEnum.id == remoteEnum.id) {
                    foundRemoteEnum = remoteEnum
                    break
                }
            }

            assertThat("No matching enumeration found for $localEnum", foundRemoteEnum, notNullValue())
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


    private fun waitTillKnownSynchronizedDeviceConnected(connectedDevicesService: IConnectedDevicesService, countDownLatch: CountDownLatch) {
        connectedDevicesService.addKnownSynchronizedDevicesListener(object : KnownSynchronizedDevicesListener {
            override fun knownSynchronizedDeviceConnected(connectedDevice: DiscoveredDevice) {
                log.info("Counting down ...")
                countDownLatch.countDown()
            }

            override fun knownSynchronizedDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
            }

        })
    }

    private fun mockDialogServiceTextInput(dialogService: IDialogService, textToReturn: AtomicReference<String>) {
        whenever(dialogService.askForTextInput(any<CharSequence>(), anyOrNull(), anyOrNull(), any())).thenAnswer { invocation ->
            val callback = invocation.arguments[3] as (Boolean, String?) -> Unit
            callback(true, textToReturn.get())
        }
    }


    private fun createDeviceRegistrationHandler(registerAtRemote: AtomicBoolean, permitRemoteToSynchronize: AtomicBoolean, correctChallengeResponse: AtomicReference<String>,
                                                dataManager: DataManager, initialSyncManager: InitialSyncManager,
                                                dialogService: IDialogService, localization: Localization): DeviceRegistrationHandlerBase {
        return object : DeviceRegistrationHandlerBase(dataManager, initialSyncManager, dialogService, localization) {
            override fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
                callback(registerAtRemote.get(), false)
            }

            override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
                callback(remoteDeviceInfo, permitRemoteToSynchronize.get())
            }

            override fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
                correctChallengeResponse.set(correctResponse)
            }

            override fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
            }
        }
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
package net.dankito.deepthought.communication

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
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
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.enums.ExtensibleEnumeration
import net.dankito.deepthought.model.enums.OsType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.summary.ArticleSummaryExtractorBase
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractor
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.service.synchronization.*
import net.dankito.service.synchronization.changeshandler.SynchronizedChangesHandler
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.ThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.services.hashing.HashService
import net.dankito.utils.services.hashing.IBase64Service
import net.dankito.utils.services.network.NetworkConnectivityManagerBase
import net.dankito.utils.services.network.NetworkHelper
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.settings.LocalSettingsStoreBase
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.version.Versions
import net.engio.mbassy.listener.Handler
import net.engio.mbassy.listener.Listener
import net.engio.mbassy.listener.References
import org.hamcrest.CoreMatchers.*
import org.hamcrest.number.OrderingComparison.*
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class CommunicationManagerTest {

    companion object {
        const val LocalDeviceName = "Local"
        val LocalOsType = OsType.ANDROID

        const val RemoteDeviceName = "Remote"
        val RemoteOsType = OsType.DESKTOP

        const val IntegrationTestDevicesDiscoveryPrefix = "DeepThought_IntegrationTest"

        const val InitializationTimeoutInSeconds = 5L
        const val FindRemoteDeviceTimeoutInSeconds = 300L // it really takes a long time till Couchbase opens its listener port

        private val log = LoggerFactory.getLogger(CommunicationManagerTest::class.java)
    }


    private val localization: Localization = Localization()

    private val base64Service: IBase64Service = mock()

    private val hashService = HashService()

    private val fileStorageService = JavaFileStorageService()

    private val serializer = JacksonJsonSerializer(mock(), mock())


    private lateinit var localDevice: Device

    private val localPlatformConfiguration = object: IPlatformConfiguration {
        override fun getUserName(): String { return "Rieka" }

        override fun getDeviceName(): String? { return LocalDeviceName }

        override fun getOsType(): OsType { return LocalOsType }

        override fun getOsName(): String { return "Android" }

        override fun getOsVersion(): Int { return 6 }

        override fun getOsVersionString(): String { return "6.0" }

        override fun getApplicationFolder(): File { return File(".").absoluteFile.parentFile }

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

    private val localDevicesDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, localThreadPool)

    private lateinit var localEntityManager: CouchbaseLiteEntityManagerBase

    private lateinit var localDataManager: DataManager

    private lateinit var localClientCommunicator: IClientCommunicator

    private val localEventBus = MBassadorEventBus()

    private val localEntityChangedNotifier = EntityChangedNotifier(localEventBus)

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

        override fun getApplicationFolder(): File { return File(".").absoluteFile.parentFile }

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

    private val remoteDevicesDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, remoteThreadPool)

    private lateinit var remoteEntityManager: CouchbaseLiteEntityManagerBase

    private lateinit var remoteDataManager: DataManager

    private lateinit var remoteClientCommunicator: IClientCommunicator

    private val remoteEventBus = MBassadorEventBus()

    private val remoteEntityChangedNotifier = EntityChangedNotifier(remoteEventBus)

    private lateinit var remoteSynchronizedChangesHandler: SynchronizedChangesHandler

    private lateinit var remoteSyncManager: CouchbaseLiteSyncManager

    private lateinit var remoteConnectedDevicesService: IConnectedDevicesService

    private lateinit var remoteCommunicationManager: ICommunicationManager


    @Before
    @Throws(Exception::class)
    fun setUp() {
        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultDataFolder())
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultDataFolder())

        whenever(base64Service.encode(any<ByteArray>())).thenReturn("fake_base64_encoded_string")

        setupLocalDevice()

        setupRemoteDevice()
    }

    private fun setupLocalDevice() {
        val entityManagerConfiguration = EntityManagerConfiguration(localPlatformConfiguration.getDefaultDataFolder().path, "test")
        localEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, createLocalSettingsStore())

        localDataManager = DataManager(localEntityManager, entityManagerConfiguration, DefaultDataInitializer(localPlatformConfiguration, localization), localPlatformConfiguration)

        localDialogService = mock<IDialogService>()
        localInitialSyncManager = InitialSyncManager(localEntityManager, localization)

        val initializationLatch = CountDownLatch(1)

        localDataManager.addInitializationListener {
            localDevice = localDataManager.localDevice
            localNetworkSettings = NetworkSettings(localDevice, localDataManager.localUser, IntegrationTestDevicesDiscoveryPrefix) // set different discovery message prefix to not interfere with production device in same local network

            localSynchronizedChangesHandler = SynchronizedChangesHandler(localEntityManager, localEntityChangedNotifier)

            localSyncManager = CouchbaseLiteSyncManager(localEntityManager, localSynchronizedChangesHandler, localNetworkSettings)

            localRegistrationHandler = createDeviceRegistrationHandler(localRegisterAtRemote, localPermitRemoteToSynchronize, localCorrectChallengeResponse, localDataManager,
                    localInitialSyncManager, localDialogService, localization)

            localClientCommunicator = TcpSocketClientCommunicator(localNetworkSettings, localRegistrationHandler, localEntityManager, serializer, base64Service, hashService, localThreadPool)

            localConnectedDevicesService = ConnectedDevicesService(localDevicesDiscoverer, localClientCommunicator, localSyncManager, localRegistrationHandler, localNetworkSettings, localEntityManager)

            localCommunicationManager = CommunicationManager(localConnectedDevicesService, localSyncManager, localClientCommunicator, localNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)
    }


    private fun setupRemoteDevice() {
        val entityManagerConfiguration = EntityManagerConfiguration(remotePlatformConfiguration.getDefaultDataFolder().path, "test")
        remoteEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, createLocalSettingsStore())

        remoteDataManager = DataManager(remoteEntityManager, entityManagerConfiguration, DefaultDataInitializer(remotePlatformConfiguration, localization), remotePlatformConfiguration)

        remoteDialogService = mock<IDialogService>()
        remoteInitialSyncManager = InitialSyncManager(remoteEntityManager, localization)

        val initializationLatch = CountDownLatch(1)

        remoteDataManager.addInitializationListener {
            remoteDevice = remoteDataManager.localDevice
            remoteNetworkSettings = NetworkSettings(remoteDevice, remoteDataManager.localUser, IntegrationTestDevicesDiscoveryPrefix)

            remoteSynchronizedChangesHandler = SynchronizedChangesHandler(remoteEntityManager, remoteEntityChangedNotifier)

            remoteSyncManager = CouchbaseLiteSyncManager(remoteEntityManager as CouchbaseLiteEntityManagerBase, remoteSynchronizedChangesHandler, remoteNetworkSettings)

            val registrationHandlerInstance = createDeviceRegistrationHandler(remoteRegisterAtRemote, remotePermitRemoteToSynchronize, remoteCorrectChallengeResponse, remoteDataManager,
                    remoteInitialSyncManager, remoteDialogService, localization)
//            remoteRegistrationHandler = spy<IDeviceRegistrationHandler>(registrationHandlerInstance)
            remoteRegistrationHandler = registrationHandlerInstance

            remoteClientCommunicator = TcpSocketClientCommunicator(remoteNetworkSettings, remoteRegistrationHandler, remoteEntityManager, serializer, base64Service, hashService, remoteThreadPool)

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

        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultDataFolder())
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultDataFolder())
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
    fun localDeviceRequestsSynchronization_FirstEnterFalseResponse_ThenEnterCorrectResponse_SynchronizationIsAllowed() {
        val wrongResponse = "not_valid"

        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)
        var countAskForChallenge = 0

        whenever(localDialogService.askForTextInput(any<CharSequence>(), anyOrNull(), anyOrNull(), any(), any())).thenAnswer { invocation ->
            val callback = invocation.arguments[4] as (Boolean, String?) -> Unit
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


    @Test
    fun localDeviceRequestsSynchronization_SynchronizedAndIgnoredDevicesGetSynchronizedCorrectly() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val localUser = localDataManager.localUser
        val remoteUser = remoteDataManager.localUser

        val localSynchronizedDevice1 = Device("Local_Synchronized_1", UUID.randomUUID().toString(), OsType.DESKTOP)
        val localSynchronizedDevice2 = Device("Local_Synchronized_2", UUID.randomUUID().toString(), OsType.DESKTOP)
        val localIgnoredDevice1 = Device("Local_Ignored_1", UUID.randomUUID().toString(), OsType.DESKTOP)
        localEntityManager.persistEntity(localSynchronizedDevice1)
        localEntityManager.persistEntity(localSynchronizedDevice2)
        localEntityManager.persistEntity(localIgnoredDevice1)
        localUser.addSynchronizedDevice(localSynchronizedDevice1)
        localUser.addSynchronizedDevice(localSynchronizedDevice2)
        localUser.addIgnoredDevice(localIgnoredDevice1)

        val remoteIgnoredDevice1 = Device("Remote_Ignored_1", UUID.randomUUID().toString(), OsType.DESKTOP)
        val remoteIgnoredDevice2 = Device("Remote_Ignored_2", UUID.randomUUID().toString(), OsType.DESKTOP)
        val remoteIgnoredDevice3 = Device("Remote_Ignored_3", UUID.randomUUID().toString(), OsType.DESKTOP)
        remoteEntityManager.persistEntity(remoteIgnoredDevice1)
        remoteEntityManager.persistEntity(remoteIgnoredDevice2)
        remoteEntityManager.persistEntity(remoteIgnoredDevice3)
        remoteUser.addIgnoredDevice(remoteIgnoredDevice1)
        remoteUser.addIgnoredDevice(remoteIgnoredDevice2)
        remoteUser.addIgnoredDevice(remoteIgnoredDevice3)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillFirstSynchronizationIsDone(remoteEventBus, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        assertThat(localUser.synchronizedDevices.size, `is`(4))
        assertThat(remoteUser.synchronizedDevices.size, `is`(4))

        assertThat(localUser.ignoredDevices.size, `is`(4))
        assertThat(remoteUser.ignoredDevices.size, `is`(4))
    }


    @Test
    fun localDeviceRequestsSynchronization_ArticleSummaryExtractorConfigsGetSynchronizedCorrectly() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        createImplementedExtractors(Mockito.mock(IWebClient::class.java)).forEach { implementedExtractor ->
            val localConfig = ArticleSummaryExtractorConfig(implementedExtractor.getUrl(), implementedExtractor.getName())
            localEntityManager.persistEntity(localConfig)

            val remoteConfig = ArticleSummaryExtractorConfig(implementedExtractor.getUrl(), implementedExtractor.getName())
            remoteEntityManager.persistEntity(remoteConfig)
        }

        // create feeds
        val localAddedFeedsCount = 2
        for(i in 1..localAddedFeedsCount) {
            localEntityManager.persistEntity(ArticleSummaryExtractorConfig("http://www.example.com/local/" + i, "local_feed_" + i))
        }
        val remoteAddedFeedsCount = 1
        for(i in 1..remoteAddedFeedsCount) {
            remoteEntityManager.persistEntity(ArticleSummaryExtractorConfig("http://www.example.com/remote/" + i, "remote_feed_" + i))
        }

        // set favorites
        val localConfigs = localEntityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)
        val remoteConfigs = remoteEntityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)

        val localFavorites = localConfigs.filterIndexed { index, articleSummaryExtractorConfig -> index % 2 == 0 }
        val remoteFavorites = remoteConfigs.filter { remoteConfig -> localFavorites.filter { it.url == remoteConfig.url }.firstOrNull() == null }.subList(0, 2)

        setFavoriteIndices(localFavorites, localEntityManager)
        setFavoriteIndices(remoteFavorites, remoteEntityManager)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillFirstSynchronizationIsDone(remoteEventBus, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        val synchronizedLocalConfigs = localEntityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)
        val synchronizedRemoteConfigs = remoteEntityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)

        assertThat(synchronizedLocalConfigs.size, `is`(localConfigs.size + remoteAddedFeedsCount))
        assertThat(synchronizedRemoteConfigs.size, `is`(remoteConfigs.size + localAddedFeedsCount))

        val synchronizedLocalFavorites = synchronizedLocalConfigs.filter { it.isFavorite }.sortedBy { it.favoriteIndex }
        val synchronizedRemoteFavorites = synchronizedRemoteConfigs.filter { it.isFavorite }.sortedBy { it.favoriteIndex }

        assertThat(synchronizedLocalFavorites.size, `is`(synchronizedRemoteFavorites.size))

        for(i in 0..synchronizedLocalFavorites.size - 1) { // assert that the have the same ordering by favoriteIndex on both sides
            val localFavorite = synchronizedLocalFavorites[i]
            val remoteFavorite = synchronizedRemoteFavorites[i]

            assertThat(localFavorite.url, `is`(remoteFavorite.url))
        }
    }

    private fun createImplementedExtractors(webClient: IWebClient): Collection<IImplementedArticleSummaryExtractor> {
        val extractors = ArrayList<IImplementedArticleSummaryExtractor>()

        extractors.add(createImplementedArticleSummaryExtractor("New York Times", "https://www.nytimes.com", webClient))
        extractors.add(createImplementedArticleSummaryExtractor("The Guardian", "https://www.guardian.co.uk", webClient))
        extractors.add(createImplementedArticleSummaryExtractor("Le Monde", "http://www.lemonde.fr", webClient))
        extractors.add(createImplementedArticleSummaryExtractor("SZ", "http://www.sz.de", webClient))

        return extractors
    }

    private fun createImplementedArticleSummaryExtractor(name: String, baseUrl: String, webClient: IWebClient): IImplementedArticleSummaryExtractor {
        return object : ArticleSummaryExtractorBase(webClient) {
            override fun getName(): String { return name  }

            override fun getUrl(): String { return baseUrl }

            override fun parseHtmlToArticleSummary(url: String, document: Document, forLoadingMoreItems: Boolean): ArticleSummary { return Mockito.mock(ArticleSummary::class.java) }
        }
    }

    private fun setFavoriteIndices(favorites: List<ArticleSummaryExtractorConfig>, entityManager: IEntityManager) {
        for(i in 0..favorites.size - 1) {
            val favorite = favorites[i]

            favorite.isFavorite = true
            favorite.favoriteIndex = i

            entityManager.updateEntity(favorite)
        }
    }


    @Test
    fun disconnect_CreateEntity_EntityGetSynchronizedCorrectly() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(localConnectedDevicesService, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        // now disconnect, ...
        localCommunicationManager.stop()
        remoteCommunicationManager.stop()

        // create Entities ...
        val newTag = Tag("New Tag")
        localEntityManager.persistEntity(newTag)

        val newReference = Source("New Source")
        localEntityManager.persistEntity(newReference)

        val newEntry = Item("New Item")
        newEntry.source = newReference
        newEntry.addTag(newTag)
        localEntityManager.persistEntity(newEntry)

        // and reconnect
        val collectedChanges = mutableListOf<EntitiesOfTypeChanged>()
        val syncAfterReconnectLatch = CountDownLatch(1)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, syncAfterReconnectLatch)

        collectSynchronizedChanges(remoteEventBus, collectedChanges)

        startCommunicationManagersAndWait(syncAfterReconnectLatch)


        assertThat(collectedChanges.size, greaterThanOrEqualTo(3))

        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Created))

        assertThat(collectedChanges.filter { it.entityType == Source::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Source::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Created))

        assertThat(collectedChanges.filter { it.entityType == Tag::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Tag::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Created))

        assertThat(remoteEntityManager.getEntityById(Item::class.java, newEntry.id!!), notNullValue())
        assertThat(remoteEntityManager.getEntityById(Item::class.java, newEntry.id!!)?.modifiedOn, `is`(newEntry.modifiedOn))

        assertThat(remoteEntityManager.getEntityById(Source::class.java, newReference.id!!), notNullValue())
        assertThat(remoteEntityManager.getEntityById(Source::class.java, newReference.id!!)?.modifiedOn, `is`(newReference.modifiedOn))

        assertThat(remoteEntityManager.getEntityById(Tag::class.java, newTag.id!!), notNullValue())
        assertThat(remoteEntityManager.getEntityById(Tag::class.java, newTag.id!!)?.modifiedOn, `is`(newTag.modifiedOn))
    }


    @Test
    fun disconnect_DeleteEntity_EntityGetSynchronizedCorrectly() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        // create Entities
        val newTag = Tag("New Tag")
        localEntityManager.persistEntity(newTag)

        val newReference = Source("New Source")
        localEntityManager.persistEntity(newReference)

        val newEntry = Item("New Item")
        newEntry.source = newReference
        newEntry.addTag(newTag)
        localEntityManager.persistEntity(newEntry)
        val entryId = newEntry.id!!

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        // now disconnect, ...
        localCommunicationManager.stop()
        remoteCommunicationManager.stop()

        // delete entity
        localEntityManager.deleteEntity(newEntry)

        // and reconnect
        val collectedChanges = mutableListOf<EntitiesOfTypeChanged>()
        val syncAfterReconnectLatch = CountDownLatch(1)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, syncAfterReconnectLatch)

        collectSynchronizedChanges(remoteEventBus, collectedChanges)

        startCommunicationManagersAndWait(syncAfterReconnectLatch)


        assertThat(collectedChanges.size, greaterThanOrEqualTo(1))
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Deleted))

        assertThat(remoteEntityManager.database.getDocument(entryId).isDeleted, `is`(true))
    }


    @Test
    fun remoteDeviceDisconnectsAndShortlyAfterReconnectsAgain_SynchronizationKeepsOnGoing() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        // create Entities
        val newTag = Tag("New Tag")
        localEntityManager.persistEntity(newTag)

        val newReference = Source("New Source")
        localEntityManager.persistEntity(newReference)

        val newEntry = Item("New Item")
        newEntry.source = newReference
        newEntry.addTag(newTag)
        localEntityManager.persistEntity(newEntry)
        val entryId = newEntry.id!!

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        // now disconnect remote, ...
        remoteCommunicationManager.stop()

        try { Thread.sleep(3000) } catch(ignored: Exception) { }

        // delete entity
        localEntityManager.deleteEntity(newEntry)

        // and reconnect
        val collectedChanges = mutableListOf<EntitiesOfTypeChanged>()
        val syncAfterReconnectLatch = CountDownLatch(1)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, syncAfterReconnectLatch)

        collectSynchronizedChanges(remoteEventBus, collectedChanges)

        remoteCommunicationManager.startAsync()
        syncAfterReconnectLatch.await(FindRemoteDeviceTimeoutInSeconds, TimeUnit.SECONDS)


        assertThat(collectedChanges.size, greaterThanOrEqualTo(1))
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Deleted))

        assertThat(remoteEntityManager.database.getDocument(entryId).isDeleted, `is`(true))
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


    private fun createLocalSettingsStore(): ILocalSettingsStore {
        return object : ILocalSettingsStore {
            override fun getDataFolder(): String {
                return LocalSettingsStoreBase.DefaultDataFolder
            }

            override fun setDataFolder(dataFolder: String) { }

            override fun getDatabaseDataModelVersion(): Int {
                return Versions.DataModelVersion
            }

            override fun setDatabaseDataModelVersion(newDataModelVersion: Int) { }

        }
    }

    private fun mockDialogServiceTextInput(dialogService: IDialogService, textToReturn: AtomicReference<String>) {
        whenever(dialogService.askForTextInput(any<CharSequence>(), anyOrNull(), anyOrNull(), any(), any())).thenAnswer { invocation ->
            val callback = invocation.arguments[4] as (Boolean, String?) -> Unit
            callback(true, textToReturn.get())
        }
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

    private fun waitTillFirstSynchronizationIsDone(eventBus: IEventBus, countDownLatch: CountDownLatch) {
        eventBus.register(WaitTillEntityOfTypeIsSynchronizedEventBusListener(User::class.java, countDownLatch)) // currently the last synchronized entity is the User
    }

    private fun waitTillEntityOfTypeIsSynchronized(eventBus: IEventBus, entityType: Class<out Any>, countDownLatch: CountDownLatch) {
        eventBus.register(WaitTillEntityOfTypeIsSynchronizedEventBusListener(entityType, countDownLatch))
    }

    @Listener(references = References.Strong)
    inner class WaitTillEntityOfTypeIsSynchronizedEventBusListener(private val entityType: Class<out Any>, private val countDownLatch: CountDownLatch) {

        @Handler
        fun entitiesOfTypeChanged(changed: EntitiesOfTypeChanged) {
            log.info("Synchronized entity of type ${changed.entityType}")
            if(changed.entityType == entityType) {
                thread {
                    Thread.sleep(10000)
                    countDownLatch.countDown()
                }
            }
        }

    }


    private fun collectSynchronizedChanges(eventBus: IEventBus, collectedChanges: MutableList<EntitiesOfTypeChanged>) {
        eventBus.register(CollectSynchronizedChangesEventBusListener(collectedChanges))
    }

    @Listener(references = References.Strong)
    inner class CollectSynchronizedChangesEventBusListener(private val collectedChanges: MutableList<EntitiesOfTypeChanged>) {

        @Handler
        fun entitiesOfTypeChanged(changed: EntitiesOfTypeChanged) {
            collectedChanges.add(changed)
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
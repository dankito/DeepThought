package net.dankito.deepthought.communication

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import net.dankito.data_access.database.DeepThoughtCouchbaseLiteEntityManagerBase
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.serialization.DeepThoughtJacksonJsonSerializer
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.summary.ArticleSummaryExtractorBase
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractor
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.synchronization.ConnectedDevicesServiceConfig
import net.dankito.synchronization.database.sync.CouchbaseLiteSyncManager
import net.dankito.synchronization.database.sync.DeepThoughtInitialSyncManager
import net.dankito.synchronization.database.sync.changeshandler.SynchronizedChangesHandler
import net.dankito.synchronization.device.discovery.udp.UdpDevicesDiscoverer
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.callback.DeepThoughtDeviceRegistrationHandlerBase
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.device.messaging.message.DeviceInfo
import net.dankito.synchronization.device.messaging.tcp.PlainTcpMessenger
import net.dankito.synchronization.device.service.*
import net.dankito.synchronization.model.*
import net.dankito.synchronization.model.enums.OsType
import net.dankito.synchronization.service.CommunicationManager
import net.dankito.synchronization.service.ICommunicationManager
import net.dankito.util.ThreadPool
import net.dankito.util.Version
import net.dankito.util.event.EntityChangeType
import net.dankito.util.hashing.HashService
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.localization.Localization
import net.dankito.util.network.NetworkConnectivityManagerBase
import net.dankito.util.network.NetworkHelper
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.util.settings.LocalSettingsStoreBase
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.util.web.IWebClient
import net.dankito.utils.PlatformConfigurationBase
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

        val AppVersion = Version(1, 0, 0)

        const val DataModelVersion = 1

        const val InitializationTimeoutInSeconds = 5L
        const val FindRemoteDeviceTimeoutInSeconds = 300L // it really takes a long time till Couchbase opens its listener port

        private val log = LoggerFactory.getLogger(CommunicationManagerTest::class.java)
    }


    private val localization: Localization = Localization("Messages")

    private val base64Service: IBase64Service = mock()

    private val hashService = HashService()

    private val fileStorageService = JavaFileStorageService()

    private val serializer = DeepThoughtJacksonJsonSerializer(mock(), mock())


    private lateinit var localDevice: Device

    private val localPlatformConfiguration = object: PlatformConfigurationBase() {
        override fun getUserName(): String { return "Rieka" }

        override fun getDeviceName(): String? { return LocalDeviceName }

        override fun getOsType(): OsType { return LocalOsType }

        override fun getOsName(): String { return "Android" }

        override fun getOsVersion(): Int { return 6 }

        override fun getOsVersionString(): String { return "6.0" }

        override fun getApplicationFolder(): File { return File(".").absoluteFile.parentFile }

        override fun getDefaultDataFolder(): File { return File(File(File("data"), "test"), "test1") }

        override fun getDefaultFilesFolder(): File { return File(getDefaultDataFolder(), FilesFolderName) }
    }

    private val localThreadPool = ThreadPool()

    private lateinit var localNetworkSettings: NetworkSettings

    private lateinit var localDialogService: IDialogService

    private lateinit var localInitialSyncManager: DeepThoughtInitialSyncManager

    private val localRegisterAtRemote = AtomicBoolean(false)
    private val localPermitRemoteToSynchronize = AtomicBoolean(false)
    private val localCorrectChallengeResponse = AtomicReference<String>()

    private lateinit var localRegistrationHandler: IDeviceRegistrationHandler

    private val localDevicesDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, localThreadPool)

    private lateinit var localEntityManager: DeepThoughtCouchbaseLiteEntityManagerBase

    private lateinit var localDataManager: DataManager

    private lateinit var localMessenger: IMessenger

    private val localEventBus = MBassadorEventBus()

    private val localEntityChangedNotifier = EntityChangedNotifier(localEventBus)

    private lateinit var localSynchronizedChangesHandler: SynchronizedChangesHandler

    private lateinit var localSyncManager: CouchbaseLiteSyncManager

    private lateinit var localDiscoveredDevicesManager: IDiscoveredDevicesManager

    private lateinit var localCommunicationManager: ICommunicationManager


    // and the same for the remote device

    private lateinit var remoteDevice: Device

    private val remotePlatformConfiguration = object: PlatformConfigurationBase() {
        override fun getUserName(): String { return "dankito" }

        override fun getDeviceName(): String? { return RemoteDeviceName }

        override fun getOsType(): OsType { return RemoteOsType }

        override fun getOsName(): String { return "Arch Linux" }

        override fun getOsVersion(): Int { return 1 }

        override fun getOsVersionString(): String { return "0.1" }

        override fun getApplicationFolder(): File { return File(".").absoluteFile.parentFile }

        override fun getDefaultDataFolder(): File { return File(File(File("data"), "test"), "test2") }

        override fun getDefaultFilesFolder(): File { return File(getDefaultDataFolder(), FilesFolderName) }
    }

    private val remoteThreadPool = ThreadPool()

    private lateinit var remoteNetworkSettings: NetworkSettings

    private lateinit var remoteDialogService: IDialogService

    private lateinit var remoteInitialSyncManager: DeepThoughtInitialSyncManager

    private val remoteRegisterAtRemote = AtomicBoolean(false)
    private val remotePermitRemoteToSynchronize = AtomicBoolean(false)
    private val remoteCorrectChallengeResponse = AtomicReference<String>()

    private lateinit var remoteRegistrationHandler: IDeviceRegistrationHandler

    private val remoteDevicesDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, remoteThreadPool)

    private lateinit var remoteEntityManager: DeepThoughtCouchbaseLiteEntityManagerBase

    private lateinit var remoteDataManager: DataManager

    private lateinit var remoteMessenger: IMessenger

    private val remoteEventBus = MBassadorEventBus()

    private val remoteEntityChangedNotifier = EntityChangedNotifier(remoteEventBus)

    private lateinit var remoteSynchronizedChangesHandler: SynchronizedChangesHandler

    private lateinit var remoteSyncManager: CouchbaseLiteSyncManager

    private lateinit var remoteDiscoveredDevicesManager: IDiscoveredDevicesManager

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
        localInitialSyncManager = DeepThoughtInitialSyncManager(localEntityManager, localization)

        val initializationLatch = CountDownLatch(1)

        localDataManager.addInitializationListener {
            localDevice = localDataManager.localDevice
            localNetworkSettings = NetworkSettings(localDevice, localDataManager.localUser, IntegrationTestDevicesDiscoveryPrefix, AppVersion, DataModelVersion) // set different discovery message prefix to not interfere with production device in same local network

            localSynchronizedChangesHandler = SynchronizedChangesHandler(localEntityManager, localEntityChangedNotifier)

            localSyncManager = CouchbaseLiteSyncManager(localEntityManager, localSynchronizedChangesHandler, localNetworkSettings)

            localRegistrationHandler = createDeviceRegistrationHandler(localRegisterAtRemote, localPermitRemoteToSynchronize, localCorrectChallengeResponse, localDataManager,
                    localNetworkSettings, localInitialSyncManager, localDialogService, localization)

            localMessenger = PlainTcpMessenger(localNetworkSettings, localRegistrationHandler, localEntityManager, serializer, base64Service, hashService, localThreadPool)

            localDiscoveredDevicesManager = DiscoveredDevicesManager(localDevicesDiscoverer, localMessenger, localSyncManager, localRegistrationHandler, localNetworkSettings,
                    localEntityManager, ConnectedDevicesServiceConfig.DEVICES_DISCOVERER_PORT, ConnectedDevicesServiceConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS)

            localCommunicationManager = CommunicationManager(localDiscoveredDevicesManager, localSyncManager, localMessenger, localNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)
    }


    private fun setupRemoteDevice() {
        val entityManagerConfiguration = EntityManagerConfiguration(remotePlatformConfiguration.getDefaultDataFolder().path, "test")
        remoteEntityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, createLocalSettingsStore())

        remoteDataManager = DataManager(remoteEntityManager, entityManagerConfiguration, DefaultDataInitializer(remotePlatformConfiguration, localization), remotePlatformConfiguration)

        remoteDialogService = mock<IDialogService>()
        remoteInitialSyncManager = DeepThoughtInitialSyncManager(remoteEntityManager, localization)

        val initializationLatch = CountDownLatch(1)

        remoteDataManager.addInitializationListener {
            remoteDevice = remoteDataManager.localDevice
            remoteNetworkSettings = NetworkSettings(remoteDevice, remoteDataManager.localUser, IntegrationTestDevicesDiscoveryPrefix, AppVersion, DataModelVersion)

            remoteSynchronizedChangesHandler = SynchronizedChangesHandler(remoteEntityManager, remoteEntityChangedNotifier)

            remoteSyncManager = CouchbaseLiteSyncManager(remoteEntityManager as DeepThoughtCouchbaseLiteEntityManagerBase, remoteSynchronizedChangesHandler, remoteNetworkSettings)

            val registrationHandlerInstance = createDeviceRegistrationHandler(remoteRegisterAtRemote, remotePermitRemoteToSynchronize, remoteCorrectChallengeResponse, remoteDataManager,
                    remoteNetworkSettings, remoteInitialSyncManager, remoteDialogService, localization)
//            remoteRegistrationHandler = spy<IDeviceRegistrationHandler>(registrationHandlerInstance)
            remoteRegistrationHandler = registrationHandlerInstance

            remoteMessenger = PlainTcpMessenger(remoteNetworkSettings, remoteRegistrationHandler, remoteEntityManager, serializer, base64Service, hashService, remoteThreadPool)

            remoteDiscoveredDevicesManager = DiscoveredDevicesManager(remoteDevicesDiscoverer, remoteMessenger, remoteSyncManager, remoteRegistrationHandler, remoteNetworkSettings,
                    remoteEntityManager, ConnectedDevicesServiceConfig.DEVICES_DISCOVERER_PORT, ConnectedDevicesServiceConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS)

            remoteCommunicationManager = CommunicationManager(remoteDiscoveredDevicesManager, remoteSyncManager, remoteMessenger, remoteNetworkSettings)

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

        localDiscoveredDevicesManager.addDiscoveredDevicesListener(createDiscoveredDevicesListener(localDiscoveredDevicesList, countDownLatch))

        remoteDiscoveredDevicesManager.addDiscoveredDevicesListener(createDiscoveredDevicesListener(remoteDiscoveredDevicesList, countDownLatch))

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

        waitTillKnownSynchronizedDeviceConnected(localDiscoveredDevicesManager, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(localDiscoveredDevicesManager.knownSynchronizedDiscoveredDevices.size, `is`(1))

        assertThat(remoteNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(remoteDiscoveredDevicesManager.knownSynchronizedDiscoveredDevices.size, `is`(1))
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

        waitTillKnownSynchronizedDeviceConnected(localDiscoveredDevicesManager, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(localDiscoveredDevicesManager.knownSynchronizedDiscoveredDevices.size, `is`(1))

        assertThat(remoteNetworkSettings.synchronizationPort, greaterThan(1023))
        assertThat(remoteDiscoveredDevicesManager.knownSynchronizedDiscoveredDevices.size, `is`(1))
    }


    @Test
    fun localDeviceRequestsSynchronization_RemoteDeniesSynchronization() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(false)

        val countDownLatch = CountDownLatch(1)

        localNetworkSettings.addListener(object : NetworkSettingsChangedListener {
            override fun settingsChanged(networkSettings: NetworkSettings, setting: NetworkSetting, newValue: Any, oldValue: Any?) {
                if(setting == NetworkSetting.REMOVED_DEVICES_ASKED_FOR_PERMITTING_SYNCHRONIZATION) {
                    println("Counting down ...")
                    countDownLatch.countDown()
                }
            }
        })

        startCommunicationManagersAndWait(countDownLatch)

        assertThat(localNetworkSettings.synchronizationPort, lessThanOrEqualTo(0))
        assertThat(localDiscoveredDevicesManager.knownSynchronizedDiscoveredDevices.size, `is`(0))

        assertThat(remoteNetworkSettings.synchronizationPort, lessThanOrEqualTo(0))
        assertThat(remoteDiscoveredDevicesManager.knownSynchronizedDiscoveredDevices.size, `is`(0))
    }


    @Test
    fun localDeviceRequestsSynchronization_RemoteInfoIsUsedForInitialSynchronization() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(localDiscoveredDevicesManager, countDownLatch)


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
    }


    @Test
    fun remoteDeviceRequestsSynchronization_LocalInfoIsUsedForInitialSynchronization() {
        remoteRegisterAtRemote.set(true)
        localPermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(remoteDialogService, localCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(remoteDiscoveredDevicesManager, countDownLatch)


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

        waitTillKnownSynchronizedDeviceConnected(localDiscoveredDevicesManager, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        // now disconnect, ...
        localCommunicationManager.stop()
        remoteCommunicationManager.stop()

        // create Entities ...
        val newTag = Tag("New Tag")
        localEntityManager.persistEntity(newTag)

        val newSource = Source("New Source")
        localEntityManager.persistEntity(newSource)

        val newItem = Item("New Item")
        newItem.source = newSource
        newItem.addTag(newTag)
        localEntityManager.persistEntity(newItem)

        // and reconnect
        val collectedChanges = mutableListOf<EntitiesOfTypeChanged>()
        val syncAfterReconnectLatch = CountDownLatch(1)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, syncAfterReconnectLatch)

        collectSynchronizedChanges(remoteEventBus, collectedChanges)

        startCommunicationManagersAndWait(syncAfterReconnectLatch)


        assertThat(collectedChanges.size, greaterThanOrEqualTo(3))

        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Updated))

        assertThat(collectedChanges.filter { it.entityType == Source::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Source::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Updated))

        assertThat(collectedChanges.filter { it.entityType == Tag::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Tag::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Created))

        assertThat(remoteEntityManager.getEntityById(Item::class.java, newItem.id!!), notNullValue())
        assertThat(remoteEntityManager.getEntityById(Item::class.java, newItem.id!!)?.modifiedOn, `is`(newItem.modifiedOn))

        assertThat(remoteEntityManager.getEntityById(Source::class.java, newSource.id!!), notNullValue())
        assertThat(remoteEntityManager.getEntityById(Source::class.java, newSource.id!!)?.modifiedOn, `is`(newSource.modifiedOn))

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

        val newSource = Source("New Source")
        localEntityManager.persistEntity(newSource)

        val newItem = Item("New Item")
        newItem.source = newSource
        newItem.addTag(newTag)
        localEntityManager.persistEntity(newItem)
        val itemId = newItem.id!!

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        // now disconnect, ...
        localCommunicationManager.stop()
        remoteCommunicationManager.stop()

        // delete entity
        localEntityManager.deleteEntity(newItem)

        // and reconnect
        val collectedChanges = mutableListOf<EntitiesOfTypeChanged>()
        val syncAfterReconnectLatch = CountDownLatch(1)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, syncAfterReconnectLatch)

        collectSynchronizedChanges(remoteEventBus, collectedChanges)

        startCommunicationManagersAndWait(syncAfterReconnectLatch)


        assertThat(collectedChanges.size, greaterThanOrEqualTo(1))
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull(), notNullValue())
        assertThat(collectedChanges.filter { it.entityType == Item::class.java }.firstOrNull()?.changeType, `is`(EntityChangeType.Deleted))

        assertThat(remoteEntityManager.database.getDocument(itemId).isDeleted, `is`(true))
    }


    @Test
    fun remoteDeviceDisconnectsAndShortlyAfterReconnectsAgain_SynchronizationKeepsOnGoing() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        // create Entities
        val newTag = Tag("New Tag")
        localEntityManager.persistEntity(newTag)

        val newSource = Source("New Source")
        localEntityManager.persistEntity(newSource)

        val newItem = Item("New Item")
        newItem.source = newSource
        newItem.addTag(newTag)
        localEntityManager.persistEntity(newItem)
        val itemId = newItem.id!!

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillEntityOfTypeIsSynchronized(remoteEventBus, Item::class.java, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)


        // now disconnect remote, ...
        remoteCommunicationManager.stop()

        try { Thread.sleep(3000) } catch(ignored: Exception) { }

        // delete entity
        localEntityManager.deleteEntity(newItem)

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

        assertThat(remoteEntityManager.database.getDocument(itemId).isDeleted, `is`(true))
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

    private fun waitTillKnownSynchronizedDeviceConnected(discoveredDevicesManager: IDiscoveredDevicesManager, countDownLatch: CountDownLatch) {
        discoveredDevicesManager.addKnownSynchronizedDevicesListener(object : KnownSynchronizedDevicesListener {
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
                                                dataManager: DataManager, networkSettings: NetworkSettings, initialSyncManager: DeepThoughtInitialSyncManager,
                                                dialogService: IDialogService, localization: Localization): IDeviceRegistrationHandler {
        return object : DeepThoughtDeviceRegistrationHandlerBase(dataManager.deepThought, dataManager.entityManager, networkSettings, initialSyncManager, dialogService, localization) {
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
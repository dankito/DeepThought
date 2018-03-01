package net.dankito.deepthought.files.synchronization

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import net.dankito.data_access.database.DeepThoughtCouchbaseLiteEntityManagerBase
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.deepthought.communication.CommunicationManager
import net.dankito.deepthought.communication.ICommunicationManager
import net.dankito.deepthought.data.FilePersister
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.MimeTypeService
import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.deepthought.service.permissions.JavaPermissionsService
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.messages.EntitiesOfTypeChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.LuceneSearchEngine
import net.dankito.synchronization.database.sync.CouchbaseLiteSyncManager
import net.dankito.synchronization.database.sync.changeshandler.ISynchronizedChangesHandler
import net.dankito.synchronization.database.sync.changeshandler.SynchronizedChangesHandler
import net.dankito.synchronization.ConnectedDevicesServiceConfig
import net.dankito.synchronization.database.sync.DeepThoughtInitialSyncManager
import net.dankito.synchronization.database.sync.ISyncManager
import net.dankito.synchronization.device.discovery.udp.UdpDevicesDiscoverer
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.callback.DeepThoughtDeviceRegistrationHandlerBase
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.device.messaging.message.DeviceInfo
import net.dankito.synchronization.device.messaging.tcp.PlainTcpMessenger
import net.dankito.synchronization.device.service.ConnectedDevicesService
import net.dankito.synchronization.device.service.IConnectedDevicesService
import net.dankito.synchronization.device.service.KnownSynchronizedDevicesListener
import net.dankito.synchronization.model.Device
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.synchronization.model.User
import net.dankito.synchronization.model.enums.OsType
import net.dankito.util.ThreadPool
import net.dankito.util.event.EntityChangeSource
import net.dankito.util.event.EntityChangeType
import net.dankito.util.hashing.HashService
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.localization.Localization
import net.dankito.util.network.NetworkConnectivityManagerBase
import net.dankito.util.network.NetworkHelper
import net.dankito.util.network.SocketHandler
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.util.settings.LocalSettingsStoreBase
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.utils.OsHelper
import net.dankito.utils.PlatformConfigurationBase
import net.dankito.utils.language.NoOpLanguageDetector
import net.dankito.utils.serialization.DeepThoughtJacksonJsonSerializer
import net.dankito.utils.version.Versions
import net.engio.mbassy.listener.Handler
import net.engio.mbassy.listener.Listener
import net.engio.mbassy.listener.References
import org.junit.After
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread


abstract class FileSyncServiceIntegrationTestBase {

    companion object {
        const val LocalDeviceName = "Local"
        val LocalOsType = OsType.ANDROID

        const val RemoteDeviceName = "Remote"
        val RemoteOsType = OsType.DESKTOP

        const val IntegrationTestDevicesDiscoveryPrefix = "DeepThought_FileSyncService_IntegrationTest"

        const val InitializationTimeoutInSeconds = 10L
        const val FindRemoteDeviceTimeoutInSeconds = 300L // it really takes a long time till Couchbase opens its listener port
        const val SynchronizeEntityTimeoutInSeconds = 30L

        private val log = LoggerFactory.getLogger(FileSyncServiceIntegrationTestBase::class.java)
    }


    protected val localization = Localization("Messages")

    protected val base64Service: IBase64Service = mock()

    protected val hashService = HashService()

    protected val mimeTypeDetector = MimeTypeDetector()

    protected val mimeTypeCategorizer = MimeTypeCategorizer()

    protected val fileStorageService = JavaFileStorageService()



    protected val localPlatformConfiguration = object: PlatformConfigurationBase() {
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


    protected lateinit var localMimeTypeService: MimeTypeService

    protected lateinit var localFileServer: FileServer

    protected lateinit var localFileSyncService: FileSyncService

    protected lateinit var localFileManager: FileManager


    protected val localSocketHandler = SocketHandler()

    protected val localSerializer = DeepThoughtJacksonJsonSerializer(mock(), mock()) // in this case we don't need TagService and SeriesService

    protected val localThreadPool = ThreadPool()

    protected val localEventBus = MBassadorEventBus()

    protected val localEntityChangedNotifier = EntityChangedNotifier(localEventBus)

    protected val localEntityManagerConfiguration = EntityManagerConfiguration(localPlatformConfiguration.getDefaultDataFolder().path, "test")

    protected val localEntityManager = JavaCouchbaseLiteEntityManager(localEntityManagerConfiguration, createLocalSettingsStore())

    protected val localDialogService = mock<IDialogService>()

    protected val localInitialSyncManager = DeepThoughtInitialSyncManager(localEntityManager, localization)

    protected val localRegisterAtRemote = AtomicBoolean(false)
    protected val localPermitRemoteToSynchronize = AtomicBoolean(false)
    protected val localCorrectChallengeResponse = AtomicReference<String>()

    protected val localDevicesDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, localThreadPool)


    protected lateinit var localDataManager: DataManager

    protected lateinit var localItemService: ItemService

    protected lateinit var localTagService: TagService

    protected lateinit var localSourceService: SourceService

    protected lateinit var localSeriesService: SeriesService

    protected lateinit var localReadLaterArticleService: ReadLaterArticleService

    protected lateinit var localLocalFileInfoService: LocalFileInfoService

    protected lateinit var localFileService: FileService

    protected lateinit var localFilePersister: FilePersister

    protected lateinit var localSearchEngine: ISearchEngine


    protected lateinit var localDevice: Device

    protected lateinit var localNetworkSettings: NetworkSettings

    protected lateinit var localSynchronizedChangesHandler: ISynchronizedChangesHandler

    protected lateinit var localSyncManager: ISyncManager

    protected lateinit var localRegistrationHandler : IDeviceRegistrationHandler

    protected lateinit var localMessenger: IMessenger

    protected lateinit var localConnectedDevicesService: IConnectedDevicesService

    protected lateinit var localCommunicationManager: ICommunicationManager



    // and the same for the remote device

    protected val remotePlatformConfiguration = object: PlatformConfigurationBase() {
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


    protected lateinit var remoteMimeTypeService: MimeTypeService

    protected lateinit var remoteFileServer: FileServer

    protected lateinit var remoteFileSyncService: FileSyncService

    protected lateinit var remoteFileManager: FileManager


    protected val remoteSocketHandler = SocketHandler()

    protected val remoteSerializer = DeepThoughtJacksonJsonSerializer(mock(), mock()) // in this case we don't need TagService and SeriesService

    protected val remoteThreadPool = ThreadPool()

    protected val remoteEventBus = MBassadorEventBus()

    protected val remoteEntityChangedNotifier = EntityChangedNotifier(remoteEventBus)

    protected val remoteEntityManagerConfiguration = EntityManagerConfiguration(remotePlatformConfiguration.getDefaultDataFolder().path, "test")

    protected val remoteEntityManager = JavaCouchbaseLiteEntityManager(remoteEntityManagerConfiguration, createLocalSettingsStore())

    protected val remoteDialogService = mock<IDialogService>()

    protected val remoteInitialSyncManager = DeepThoughtInitialSyncManager(remoteEntityManager, localization)

    protected val remoteRegisterAtRemote = AtomicBoolean(false)
    protected val remotePermitRemoteToSynchronize = AtomicBoolean(false)
    protected val remoteCorrectChallengeResponse = AtomicReference<String>()

    protected val remoteDevicesDiscoverer = UdpDevicesDiscoverer(object : NetworkConnectivityManagerBase(NetworkHelper()) { }, remoteThreadPool)


    protected lateinit var remoteDataManager: DataManager

    protected lateinit var remoteItemService: ItemService

    protected lateinit var remoteTagService: TagService

    protected lateinit var remoteSourceService: SourceService

    protected lateinit var remoteSeriesService: SeriesService

    protected lateinit var remoteReadLaterArticleService: ReadLaterArticleService

    protected lateinit var remoteLocalFileInfoService: LocalFileInfoService

    protected lateinit var remoteFileService: FileService

    protected lateinit var remoteFilePersister: FilePersister

    protected lateinit var remoteSearchEngine: ISearchEngine


    protected lateinit var remoteDevice: Device

    protected lateinit var remoteNetworkSettings: NetworkSettings

    protected lateinit var remoteSynchronizedChangesHandler: ISynchronizedChangesHandler

    protected lateinit var remoteSyncManager: ISyncManager

    protected lateinit var remoteRegistrationHandler : IDeviceRegistrationHandler

    protected lateinit var remoteMessenger: IMessenger

    protected lateinit var remoteConnectedDevicesService: IConnectedDevicesService

    protected lateinit var remoteCommunicationManager: ICommunicationManager



    init {
        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultDataFolder())
        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultFilesFolder())
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultDataFolder())
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultFilesFolder())

        whenever(base64Service.encode(any<ByteArray>())).thenReturn("fake_base64_encoded_string")

        setupLocalDevice()

        setupRemoteDevice()
    }

    private fun setupLocalDevice() {
        val initializationLatch = CountDownLatch(1)

        localDataManager = DataManager(localEntityManager, localEntityManagerConfiguration, DefaultDataInitializer(localPlatformConfiguration, localization), localPlatformConfiguration)

        localItemService = ItemService(localDataManager, localEntityChangedNotifier)

        localTagService = TagService(localDataManager, localEntityChangedNotifier)

        localSourceService = SourceService(localDataManager, localEntityChangedNotifier)

        localSeriesService = SeriesService(localDataManager, localEntityChangedNotifier)

        localReadLaterArticleService = ReadLaterArticleService(localDataManager, localEntityChangedNotifier, localSerializer)

        localLocalFileInfoService = LocalFileInfoService(localDataManager, localEntityChangedNotifier)

        localFileService = FileService(localDataManager, localEntityChangedNotifier)

        localSearchEngine = LuceneSearchEngine(localDataManager, mock(), NoOpLanguageDetector(), OsHelper(localPlatformConfiguration), localThreadPool, localEventBus,
                localItemService, localTagService, localSourceService, localSeriesService, localReadLaterArticleService, localFileService, localLocalFileInfoService)

        localDataManager.addInitializationListener {
            localDevice = localDataManager.localDevice
            localNetworkSettings = NetworkSettings(localDevice, localDataManager.localUser, IntegrationTestDevicesDiscoveryPrefix, Versions.AppVersion, Versions.DataModelVersion) // set different discovery message prefix to not interfere with production device in same local network

            localSynchronizedChangesHandler = SynchronizedChangesHandler(localEntityManager, localEntityChangedNotifier)

            localSyncManager = CouchbaseLiteSyncManager(localEntityManager, localSynchronizedChangesHandler, localNetworkSettings)

            localRegistrationHandler = createDeviceRegistrationHandler(localRegisterAtRemote, localPermitRemoteToSynchronize, localCorrectChallengeResponse, localDataManager,
                    localNetworkSettings, localInitialSyncManager, localDialogService, localization)

            localMessenger = PlainTcpMessenger(localNetworkSettings, localRegistrationHandler, localEntityManager, localSerializer, base64Service, hashService, localThreadPool)

            localConnectedDevicesService = ConnectedDevicesService(localDevicesDiscoverer, localMessenger, localSyncManager, localRegistrationHandler, localNetworkSettings,
                    localEntityManager, ConnectedDevicesServiceConfig.DEVICES_DISCOVERER_PORT, ConnectedDevicesServiceConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS)

            localCommunicationManager = CommunicationManager(localConnectedDevicesService, localSyncManager, localMessenger, localNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)


        localMimeTypeService = MimeTypeService(mimeTypeDetector, mimeTypeCategorizer, localDataManager)

        localFileServer = FileServer(localSearchEngine, localEntityManager, localNetworkSettings, localSocketHandler, localSerializer, localThreadPool)

        localFileSyncService = FileSyncService(localConnectedDevicesService, localSearchEngine,
                localSocketHandler, localLocalFileInfoService, localSerializer, JavaPermissionsService(), localPlatformConfiguration, hashService
        )

        localFileManager = FileManager(localSearchEngine, localLocalFileInfoService, localFileSyncService, localMimeTypeService, hashService, localEventBus, localThreadPool)

        localFilePersister = FilePersister(localFileService, localFileManager, localThreadPool)
    }

    private fun setupRemoteDevice() {
        val initializationLatch = CountDownLatch(1)

        remoteDataManager = DataManager(remoteEntityManager, remoteEntityManagerConfiguration, DefaultDataInitializer(remotePlatformConfiguration, localization), remotePlatformConfiguration)

        remoteItemService = ItemService(remoteDataManager, remoteEntityChangedNotifier)

        remoteTagService = TagService(remoteDataManager, remoteEntityChangedNotifier)

        remoteSourceService = SourceService(remoteDataManager, remoteEntityChangedNotifier)

        remoteSeriesService = SeriesService(remoteDataManager, remoteEntityChangedNotifier)

        remoteReadLaterArticleService = ReadLaterArticleService(remoteDataManager, remoteEntityChangedNotifier, remoteSerializer)

        remoteLocalFileInfoService = LocalFileInfoService(remoteDataManager, remoteEntityChangedNotifier)

        remoteFileService = FileService(remoteDataManager, remoteEntityChangedNotifier)

        remoteSearchEngine = LuceneSearchEngine(remoteDataManager, mock(), NoOpLanguageDetector(), OsHelper(remotePlatformConfiguration), remoteThreadPool, remoteEventBus,
                remoteItemService, remoteTagService, remoteSourceService, remoteSeriesService, remoteReadLaterArticleService, remoteFileService, remoteLocalFileInfoService)

        remoteDataManager.addInitializationListener {
            remoteDevice = remoteDataManager.localDevice
            remoteNetworkSettings = NetworkSettings(remoteDevice, remoteDataManager.localUser, IntegrationTestDevicesDiscoveryPrefix, Versions.AppVersion, Versions.DataModelVersion)

            remoteSynchronizedChangesHandler = SynchronizedChangesHandler(remoteEntityManager, remoteEntityChangedNotifier)

            remoteSyncManager = CouchbaseLiteSyncManager(remoteEntityManager as DeepThoughtCouchbaseLiteEntityManagerBase, remoteSynchronizedChangesHandler, remoteNetworkSettings)

            val registrationHandlerInstance = createDeviceRegistrationHandler(remoteRegisterAtRemote, remotePermitRemoteToSynchronize, remoteCorrectChallengeResponse, remoteDataManager,
                    remoteNetworkSettings, remoteInitialSyncManager, remoteDialogService, localization)
//            remoteRegistrationHandler = spy<IDeviceRegistrationHandler>(registrationHandlerInstance)
            remoteRegistrationHandler = registrationHandlerInstance

            remoteMessenger = PlainTcpMessenger(remoteNetworkSettings, remoteRegistrationHandler, remoteEntityManager, remoteSerializer, base64Service, hashService, remoteThreadPool)

            remoteConnectedDevicesService = ConnectedDevicesService(remoteDevicesDiscoverer, remoteMessenger, remoteSyncManager, remoteRegistrationHandler, remoteNetworkSettings,
                    remoteEntityManager, ConnectedDevicesServiceConfig.DEVICES_DISCOVERER_PORT, ConnectedDevicesServiceConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS)

            remoteCommunicationManager = CommunicationManager(remoteConnectedDevicesService, remoteSyncManager, remoteMessenger, remoteNetworkSettings)

            initializationLatch.countDown()
        }

        initializationLatch.await(InitializationTimeoutInSeconds, TimeUnit.SECONDS)


        remoteMimeTypeService = MimeTypeService(mimeTypeDetector, mimeTypeCategorizer, remoteDataManager)

        remoteFileServer = FileServer(remoteSearchEngine, remoteEntityManager, remoteNetworkSettings, remoteSocketHandler, remoteSerializer, remoteThreadPool)

        remoteFileSyncService = FileSyncService(remoteConnectedDevicesService, remoteSearchEngine,
                remoteSocketHandler, remoteLocalFileInfoService, remoteSerializer, JavaPermissionsService(), remotePlatformConfiguration, hashService
        )

        remoteFileManager = FileManager(remoteSearchEngine, remoteLocalFileInfoService, remoteFileSyncService, remoteMimeTypeService, hashService, remoteEventBus, remoteThreadPool)

        remoteFilePersister = FilePersister(remoteFileService, remoteFileManager, remoteThreadPool)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        localCommunicationManager.stop()
        localEntityManager.close()

        remoteCommunicationManager.stop()
        remoteEntityManager.close()

        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultDataFolder())
        fileStorageService.deleteFolderRecursively(localPlatformConfiguration.getDefaultFilesFolder())
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultDataFolder())
        fileStorageService.deleteFolderRecursively(remotePlatformConfiguration.getDefaultFilesFolder())
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

    protected fun createFile(): DeepThoughtFileLink {
        val tempFile = File.createTempFile("FileSyncServiceIntegrationTest", ".tmp")
        tempFile.deleteOnExit()

        val writer = FileOutputStream(tempFile).bufferedWriter()
        writer.write("Liebe") // write any content so that file size != 0
        writer.close()

        val file = localFileManager.createLocalFile(tempFile)
        localFilePersister.saveFile(file)

        return file
    }



    protected fun startCommunicationManagersAndWait(countDownLatch: CountDownLatch) {
        startCommunicationManagersAndWait(countDownLatch, FindRemoteDeviceTimeoutInSeconds)
    }

    protected fun startCommunicationManagersAndWait(countDownLatch: CountDownLatch, timeoutInMillis: Long) {
        startCommunicationManagers()

        countDownLatch.await(timeoutInMillis, TimeUnit.SECONDS)
    }

    protected fun startCommunicationManagers() {
        val waitLatch = CountDownLatch(2)

        localFileServer.startServerAsync {
            localCommunicationManager.startAsync()
            waitLatch.countDown()
        }

        remoteFileServer.startServerAsync {
            remoteCommunicationManager.startAsync()
            waitLatch.countDown()
        }

        try { waitLatch.await(30, TimeUnit.SECONDS) } catch(ignored: Exception) { }
    }

    protected fun connectDevices() {
        localRegisterAtRemote.set(true)
        remotePermitRemoteToSynchronize.set(true)

        val countDownLatch = CountDownLatch(1)

        mockDialogServiceTextInput(localDialogService, remoteCorrectChallengeResponse)

        waitTillKnownSynchronizedDeviceConnected(localConnectedDevicesService, countDownLatch)

        startCommunicationManagersAndWait(countDownLatch)
    }


    protected fun mockDialogServiceTextInput(dialogService: IDialogService, textToReturn: AtomicReference<String>) {
        whenever(dialogService.askForTextInput(any<CharSequence>(), anyOrNull(), anyOrNull(), any(), any())).thenAnswer { invocation ->
            val callback = invocation.arguments[4] as (Boolean, String?) -> Unit
            callback(true, textToReturn.get())
        }
    }

    protected fun waitTillKnownSynchronizedDeviceConnected(connectedDevicesService: IConnectedDevicesService, countDownLatch: CountDownLatch) {
        connectedDevicesService.addKnownSynchronizedDevicesListener(object : KnownSynchronizedDevicesListener {
            override fun knownSynchronizedDeviceConnected(connectedDevice: DiscoveredDevice) {
                log.info("Counting down ...")
                countDownLatch.countDown()
            }

            override fun knownSynchronizedDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
            }

        })
    }

    protected fun waitTillFirstSynchronizationIsDone(eventBus: IEventBus, countDownLatch: CountDownLatch) {
        eventBus.register(WaitTillEntityOfTypeIsSynchronizedEventBusListener(User::class.java, countDownLatch, EntityChangeSource.Synchronization)) // currently the last synchronized entity is the User
    }

    protected fun waitTillEntityOfTypeIsSynchronized(eventBus: IEventBus, entityType: Class<out Any>, countDownLatch: CountDownLatch) {
        eventBus.register(WaitTillEntityOfTypeIsSynchronizedEventBusListener(entityType, countDownLatch, EntityChangeSource.Synchronization))
    }

    protected fun waitTillEntityOfTypeChanged(eventBus: IEventBus, entityType: Class<out Any>, countDownLatch: CountDownLatch, source: EntityChangeSource? = null, changeType: EntityChangeType? = null) {
        eventBus.register(WaitTillEntityOfTypeIsSynchronizedEventBusListener(entityType, countDownLatch, source, changeType))
    }

    @Listener(references = References.Strong)
    inner class WaitTillEntityOfTypeIsSynchronizedEventBusListener(private val entityType: Class<out Any>, private val countDownLatch: CountDownLatch,
                                                                   private val source: EntityChangeSource? = null, private val changeType: EntityChangeType? = null) {

        @Handler
        fun entitiesOfTypeChanged(changed: EntitiesOfTypeChanged) {
            log.info("Synchronized entity of type ${changed.entityType}")
            if(changed.entityType == entityType) {
                if((source == null || changed.source == source) && (changeType == null || changed.changeType == changeType)) {
                    thread {
                        Thread.sleep(10000)
                        countDownLatch.countDown()
                    }
                }
            }
        }

    }

}
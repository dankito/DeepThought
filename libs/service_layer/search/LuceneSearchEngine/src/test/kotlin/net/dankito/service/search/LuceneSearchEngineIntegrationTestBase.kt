package net.dankito.service.search

import com.nhaarman.mockito_kotlin.mock
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.deepthought.data.FilePersister
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.DaggerBaseComponent
import net.dankito.synchronization.files.DeepThoughtFileManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.synchronization.service.MimeTypeService
import net.dankito.synchronization.model.FileLink
import net.dankito.synchronization.model.enums.OsType
import net.dankito.synchronization.search.ISearchEngine
import net.dankito.util.ThreadPool
import net.dankito.util.hashing.HashService
import net.dankito.util.localization.Localization
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.util.settings.LocalSettingsStoreBase
import net.dankito.utils.OsHelper
import net.dankito.utils.PlatformConfigurationBase
import net.dankito.utils.language.NoOpLanguageDetector
import net.dankito.utils.serialization.DeepThoughtJacksonJsonSerializer
import net.dankito.utils.version.Versions
import org.junit.After
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class LuceneSearchEngineIntegrationTestBase {

    protected val underTest: LuceneSearchEngine


    protected val itemService: ItemService

    protected val tagService: TagService

    protected val sourceService: SourceService

    protected val seriesService: SeriesService

    protected val readLaterArticleService: ReadLaterArticleService

    protected val localFileInfoService: LocalFileInfoService

    protected val fileService: FileService

    private val mimeTypeDetector = MimeTypeDetector()

    private val mimeTypeCategorizer = MimeTypeCategorizer()

    private val mimeTypeService: MimeTypeService

    protected val fileManager: DeepThoughtFileManager

    protected val deleteEntityService: DeleteEntityService

    protected val filePersister: FilePersister

    protected val sourcePersister: SourcePersister

    protected val itemPersister: ItemPersister

    protected val eventBus: IEventBus

    protected val threadPool = ThreadPool()


    protected val platformConfiguration = object: PlatformConfigurationBase() {
        override fun getUserName() = "User"
        override fun getDeviceName() = "Device"
        override fun getOsType() = OsType.DESKTOP
        override fun getOsName() = "OS"
        override fun getOsVersion() = 0
        override fun getOsVersionString() = "0.0"

        override fun getApplicationFolder(): File { return File(".").absoluteFile.parentFile }
        override fun getDefaultDataFolder(): File { return File(File(File("data"), "test"), "lucene") }
        override fun getDefaultFilesFolder(): File { return File(getDefaultDataFolder(), FilesFolderName) }
    }

    private val fileStorageService = JavaFileStorageService()


    init {
        val component = DaggerBaseComponent.builder().build()
        BaseComponent.component = component

        fileStorageService.deleteFolderRecursively(platformConfiguration.getDefaultDataFolder())

        val localization = Localization("Messages")
        val entityManagerConfiguration = EntityManagerConfiguration(platformConfiguration.getDefaultDataFolder().path, "lucene_test")
        val entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, createLocalSettingsStore())
        val dataManager = DataManager(entityManager, entityManagerConfiguration, DefaultDataInitializer(platformConfiguration, localization), platformConfiguration)
        initDataManager(dataManager)

        eventBus = MBassadorEventBus()
        val entityChangedNotifier = EntityChangedNotifier(eventBus)

        itemService = ItemService(dataManager, entityChangedNotifier)
        tagService = TagService(dataManager, entityChangedNotifier)
        sourceService = SourceService(dataManager, entityChangedNotifier)
        seriesService = SeriesService(dataManager, entityChangedNotifier)
        readLaterArticleService = ReadLaterArticleService(dataManager, entityChangedNotifier, DeepThoughtJacksonJsonSerializer(tagService, seriesService))
        localFileInfoService = LocalFileInfoService(dataManager, entityChangedNotifier)
        fileService = FileService(dataManager, entityChangedNotifier)
        mimeTypeService = MimeTypeService(mimeTypeDetector, mimeTypeCategorizer)

        underTest = LuceneSearchEngine(dataManager, mock(), NoOpLanguageDetector(), OsHelper(platformConfiguration), ThreadPool(), eventBus,
                itemService, tagService, sourceService, seriesService, readLaterArticleService, fileService, localFileInfoService)
        initLuceneSearchEngine(underTest)

        deleteEntityService = DeleteEntityService(itemService, tagService, sourceService, seriesService, fileService, localFileInfoService, underTest, mock(), threadPool)
        fileManager = DeepThoughtFileManager(underTest as ISearchEngine<FileLink>, localFileInfoService, mock(), mimeTypeService, HashService(), eventBus, threadPool)
        filePersister = FilePersister(fileService, fileManager, threadPool)
        sourcePersister = SourcePersister(sourceService, seriesService, filePersister, deleteEntityService)
        itemPersister = ItemPersister(itemService, sourcePersister, tagService, filePersister, deleteEntityService)
    }

    private fun initDataManager(dataManager: DataManager) {
        val initializationLatch = CountDownLatch(1)
        dataManager.addInitializationListener { initializationLatch.countDown() }
        initializationLatch.await(5, TimeUnit.SECONDS)
    }

    private fun initLuceneSearchEngine(luceneSearchEngine: LuceneSearchEngine) {
        val initializationLatch = CountDownLatch(1)
        luceneSearchEngine.addInitializationListener { initializationLatch.countDown() }
        initializationLatch.await(5, TimeUnit.SECONDS)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        underTest.close()

        fileStorageService.deleteFolderRecursively(platformConfiguration.getDefaultDataFolder())
    }


    protected fun waitTillEntityGetsIndexed() {
        try {
            Thread.sleep(1000)
        } catch (ignored: Exception) { }
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

}
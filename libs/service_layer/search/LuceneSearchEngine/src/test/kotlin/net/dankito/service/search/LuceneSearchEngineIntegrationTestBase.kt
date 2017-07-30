package net.dankito.service.search

import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.DaggerBaseComponent
import net.dankito.deepthought.model.enums.OsType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.ThreadPool
import net.dankito.utils.language.NoOpLanguageDetector
import net.dankito.utils.localization.Localization
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.settings.LocalSettingsStoreBase
import net.dankito.utils.version.Versions
import org.junit.After
import org.junit.Before
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class LuceneSearchEngineIntegrationTestBase {

    protected lateinit var underTest: LuceneSearchEngine


    protected lateinit var entryService: EntryService

    protected lateinit var tagService: TagService

    protected lateinit var referenceService: ReferenceService

    protected lateinit var readLaterArticleService: ReadLaterArticleService


    private val platformConfiguration = object: IPlatformConfiguration {
        override fun getUserName() = "User"
        override fun getDeviceName() = "Device"
        override fun getOsType() = OsType.DESKTOP
        override fun getOsName() = "OS"
        override fun getOsVersion() = 0
        override fun getOsVersionString() = "0.0"

        override fun getDefaultDataFolder(): File { return File(File(File("data"), "test"), "lucene") }
    }

    private val fileStorageService = JavaFileStorageService()


    @Before
    @Throws(Exception::class)
    fun setUp() {
        val component = DaggerBaseComponent.builder().build()
        BaseComponent.component = component

        fileStorageService.deleteFolderRecursively(platformConfiguration.getDefaultDataFolder().path)

        val localization = Localization()
        val entityManagerConfiguration = EntityManagerConfiguration(platformConfiguration.getDefaultDataFolder().path, "lucene_test")
        val entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, createLocalSettingsStore())
        val dataManager = DataManager(entityManager, entityManagerConfiguration, DefaultDataInitializer(platformConfiguration, localization), platformConfiguration)
        initDataManager(dataManager)

        val eventBus = MBassadorEventBus()
        val entityChangedNotifier = EntityChangedNotifier(eventBus)

        entryService = EntryService(dataManager, entityChangedNotifier)
        tagService = TagService(dataManager, entityChangedNotifier)
        referenceService = ReferenceService(dataManager, entityChangedNotifier)
        readLaterArticleService = ReadLaterArticleService(dataManager, entityChangedNotifier, JacksonJsonSerializer(tagService))

        underTest = LuceneSearchEngine(dataManager, NoOpLanguageDetector(), ThreadPool(), eventBus, entryService, tagService, referenceService, readLaterArticleService)
        initLuceneSearchEngine(underTest)
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

        fileStorageService.deleteFolderRecursively(platformConfiguration.getDefaultDataFolder().path)
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
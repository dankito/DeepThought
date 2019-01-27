package net.dankito.service.search

import com.nhaarman.mockito_kotlin.mock
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.deepthought.data.FilePersister
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.DaggerBaseComponent
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.MimeTypeService
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.enums.OsType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.deepthought.utils.DeepThoughtJacksonJsonSerializer
import net.dankito.deepthought.utils.DeepThoughtLocalization
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.service.search.specific.TagsSearch
import net.dankito.service.search.specific.TagsSearchResults
import net.dankito.service.search.util.SortOption
import net.dankito.utils.OsHelper
import net.dankito.utils.PlatformConfigurationBase
import net.dankito.utils.ThreadPool
import net.dankito.utils.hashing.HashService
import net.dankito.utils.language.NoOpLanguageDetector
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.settings.LocalSettingsStoreBase
import net.dankito.utils.version.Versions
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

abstract class LuceneSearchEngineIntegrationTestBase {

    companion object {
        val SourcePublishingDateFormat = SimpleDateFormat("dd.MM.yyyy")
    }


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

    protected val fileManager: FileManager

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

        val localization = DeepThoughtLocalization()
        val entityManagerConfiguration = EntityManagerConfiguration(platformConfiguration.getDefaultDataFolder().path, "lucene_test")
        val localSettingsStore = createLocalSettingsStore()
        val entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration, localSettingsStore)
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
        mimeTypeService = MimeTypeService(mimeTypeDetector, mimeTypeCategorizer, dataManager)

        underTest = LuceneSearchEngine(localSettingsStore, dataManager, NoOpLanguageDetector(), OsHelper(platformConfiguration),
                ThreadPool(), eventBus, itemService, tagService, sourceService, seriesService, readLaterArticleService, fileService, localFileInfoService)
        initLuceneSearchEngine(underTest)

        deleteEntityService = DeleteEntityService(itemService, tagService, sourceService, seriesService, fileService, localFileInfoService, underTest, mock(), threadPool)
        fileManager = FileManager(underTest, localFileInfoService, mock(), mimeTypeService, HashService(), eventBus, threadPool)
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


    protected fun createSource(title: String, publishingDateString: String, series: Series? = null): Source {
        return Source(title, "", SourcePublishingDateFormat.parse(publishingDateString), series = series)
    }


    protected fun persist(item: Item) {
        itemService.persist(item)
    }

    protected fun persist(tag: Tag) {
        tagService.persist(tag)
    }

    protected fun persist(source: Source) {
        sourceService.persist(source)
    }

    protected fun persist(series: Series) {
        seriesService.persist(series)
    }

    protected fun waitTillEntityGetsIndexed() {
        try {
            Thread.sleep(1000)
        } catch (ignored: Exception) { }
    }


    protected fun searchItems(searchTerm: String, searchInContent: Boolean = true, searchInSummary: Boolean = true,
                              searchInTags: Boolean = true,
                              searchInSource: Boolean = true, searchInFiles: Boolean = true,
                              searchOnlyItemsWithoutTags: Boolean = false,
                              itemsMustHaveTheseTags: Collection<Tag> = mutableListOf(),
                              itemsMustHaveThisSource: Source? = null, itemsMustHaveThisSeries: Series? = null,
                              itemsMustHaveTheseFiles: Collection<FileLink> = mutableListOf(),
                              sortOptions: List<SortOption> = emptyList()): List<Item>? {
        val resultHolder = AtomicReference<List<Item>?>(null)
        val waitForResultLatch = CountDownLatch(1)

        underTest.searchItems(ItemsSearch(searchTerm, searchInContent, searchInSummary, searchInTags, searchInSource, searchInFiles, searchOnlyItemsWithoutTags,
                itemsMustHaveTheseTags, itemsMustHaveThisSource, itemsMustHaveThisSeries, itemsMustHaveTheseFiles, sortOptions) { result ->
            resultHolder.set(result)

            waitForResultLatch.countDown()
        })

        try {
            waitForResultLatch.await(4, TimeUnit.SECONDS)
        } catch (ignored: Exception) { }

        return resultHolder.get()
    }


    protected fun searchTags(searchTerm: String = Search.EmptySearchTerm) : TagsSearchResults {
        val resultHolder = AtomicReference<TagsSearchResults?>(null)
        val waitForResultLatch = CountDownLatch(1)

        underTest.searchTags(TagsSearch(searchTerm) { result ->
            resultHolder.set(result)

            waitForResultLatch.countDown()
        })

        try { waitForResultLatch.await(4, TimeUnit.MINUTES) } catch (ignored: Exception) { }


        MatcherAssert.assertThat(resultHolder.get(), CoreMatchers.notNullValue())

        return resultHolder.get()!!
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

            override fun getSearchEngineIndexVersion(): Int {
                return Versions.SearchEngineIndexVersion
            }

            override fun setSearchEngineIndexVersion(newSearchIndexVersion: Int) { }

        }
    }

}
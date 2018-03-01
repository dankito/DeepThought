package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContentDetector
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.feedaddressextractor.FeedAddressExtractor
import net.dankito.jpa.couchbaselite.DeepThoughtCouchbaseLiteEntityManagerBase
import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.feed.IFeedReader
import net.dankito.newsreader.feed.RomeFeedReader
import net.dankito.newsreader.summary.IImplementedArticleSummaryExtractorsManager
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.LuceneSearchEngine
import net.dankito.synchronization.ConnectedDevicesServiceConfig
import net.dankito.synchronization.database.sync.CouchbaseLiteSyncManager
import net.dankito.synchronization.database.sync.DeepThoughtInitialSyncManager
import net.dankito.synchronization.database.sync.ISyncManager
import net.dankito.synchronization.database.sync.changeshandler.ISynchronizedChangesHandler
import net.dankito.synchronization.database.sync.changeshandler.SynchronizedChangesHandler
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.device.messaging.tcp.PlainTcpMessenger
import net.dankito.synchronization.device.service.DiscoveredDevicesManager
import net.dankito.synchronization.device.service.IDiscoveredDevicesManager
import net.dankito.synchronization.files.DeepThoughtFileManager
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.synchronization.service.CommunicationManager
import net.dankito.synchronization.service.ICommunicationManager
import net.dankito.synchronization.service.MimeTypeService
import net.dankito.util.IThreadPool
import net.dankito.util.filesystem.IFileStorageService
import net.dankito.util.hashing.HashService
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.localization.Localization
import net.dankito.util.network.NetworkHelper
import net.dankito.util.serialization.ISerializer
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.util.web.IWebClient
import net.dankito.util.web.OkHttpWebClient
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.ImageCache
import net.dankito.utils.OsHelper
import net.dankito.utils.database.IDatabaseUtil
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.version.Versions
import javax.inject.Singleton


@Module
open class CommonModule {

    @Provides
    @Singleton
    open fun provideLocalization() : Localization {
        return Localization("Messages")
    }


    @Provides
    @Singleton
    open fun provideWebClient() : IWebClient {
        return OkHttpWebClient()
    }

    @Provides
    @Singleton
    open fun provideNetworkHelper() : NetworkHelper {
        return NetworkHelper()
    }

    @Provides
    @Singleton
    open fun provideOptionsForClipboardContentDetector(articleExtractorManager: ArticleExtractorManager, fileManager: DeepThoughtFileManager, dialogService: IDialogService,
                                                       mimeTypeService: MimeTypeService, platformConfiguration: IPlatformConfiguration, router: IRouter) : OptionsForClipboardContentDetector {
        return OptionsForClipboardContentDetector(articleExtractorManager, fileManager, dialogService, mimeTypeService, platformConfiguration, router)
    }


    @Provides
    @Singleton
    open fun provideOsHelper(platformConfiguration: IPlatformConfiguration) : OsHelper {
        return OsHelper(platformConfiguration)
    }

    @Provides
    @Singleton
    fun provideMimeTypeService(mimeTypeDetector: MimeTypeDetector, mimeTypeCategorizer: MimeTypeCategorizer) : MimeTypeService {
        return MimeTypeService(mimeTypeDetector, mimeTypeCategorizer)
    }

    @Provides
    @Singleton
    open fun provideImageCache(webClient: IWebClient, serializer: ISerializer, fileStorageService: IFileStorageService) : ImageCache {
        return ImageCache(webClient,serializer, fileStorageService)
    }

    @Provides
    @Singleton
    open fun provideTagsSearchResultsUtil() : TagsSearchResultsUtil {
        return TagsSearchResultsUtil()
    }


    @Provides
    @Singleton
    open fun provideSearchEngine(dataManager: DataManager, databaseUtil: IDatabaseUtil, languageDetector: ILanguageDetector, threadPool: IThreadPool, osHelper: OsHelper, eventBus: IEventBus,
                                 itemService: ItemService, tagService: TagService, sourceService: SourceService, seriesService: SeriesService,
                                 readLaterArticleService: ReadLaterArticleService, fileService: FileService, localFileInfoService: LocalFileInfoService) : ISearchEngine {
        return LuceneSearchEngine(dataManager, databaseUtil, languageDetector, osHelper, threadPool, eventBus, itemService, tagService, sourceService, seriesService,
                readLaterArticleService, fileService, localFileInfoService)
    }


    @Provides
    @Singleton
    open fun provideFaviconExtractor(webClient: IWebClient) : FaviconExtractor {
        return FaviconExtractor(webClient)
    }

    @Provides
    @Singleton
    open fun provideFaviconComparator(webClient: IWebClient) : FaviconComparator {
        return FaviconComparator(webClient)
    }


    @Provides
    @Singleton
    open fun provideArticleSummaryExtractorConfigManager(extractorManager: IImplementedArticleSummaryExtractorsManager, configService: ArticleSummaryExtractorConfigService, feedReader: IFeedReader)
            : ArticleSummaryExtractorConfigManager {
        return ArticleSummaryExtractorConfigManager(extractorManager, configService, feedReader)
    }

    @Provides
    @Singleton
    open fun provideArticleExtractors(webClient: IWebClient) : ArticleExtractors {
        return ArticleExtractors(webClient)
    }


    @Provides
    @Singleton
    open fun provideFeedAddressExtractor(webClient: IWebClient) : FeedAddressExtractor {
        return FeedAddressExtractor(webClient)
    }

    @Provides
    @Singleton
    open fun provideFeedReader(webClient: IWebClient) : IFeedReader {
        return RomeFeedReader(webClient)
    }


    @Provides
    @Singleton
    open fun provideNetworkSettings(dataManager: DataManager) : NetworkSettings {
        return NetworkSettings(dataManager.localDevice, dataManager.localUser, ConnectedDevicesServiceConfig.DefaultDeviceDiscoveryMessagePrefix, Versions.AppVersion, Versions.DataModelVersion)
    }

    @Provides
    @Singleton
    open fun provideMessenger(networkSettings: NetworkSettings, registrationHandler: IDeviceRegistrationHandler, entityManager: IEntityManager,
                                       serializer: ISerializer, base64Service: IBase64Service, hashService: HashService, threadPool: IThreadPool) : IMessenger {
        return PlainTcpMessenger(networkSettings, registrationHandler, entityManager, serializer, base64Service, hashService, threadPool)
    }

    @Provides
    @Singleton
    open fun provideInitialSyncManager(entityManager: IEntityManager, localization: Localization) : DeepThoughtInitialSyncManager {
        return DeepThoughtInitialSyncManager(entityManager, localization)
    }

    @Provides
    @Singleton
    open fun provideSynchronizedChangesHandler(entityManager: IEntityManager, changesNotifier: EntityChangedNotifier) : ISynchronizedChangesHandler {
        return SynchronizedChangesHandler(entityManager as DeepThoughtCouchbaseLiteEntityManagerBase, changesNotifier)
    }

    @Provides
    @Singleton
    open fun provideSyncManager(entityManager: IEntityManager, changesHandler: ISynchronizedChangesHandler, networkSettings: NetworkSettings) : ISyncManager {
        return CouchbaseLiteSyncManager(entityManager as DeepThoughtCouchbaseLiteEntityManagerBase, changesHandler, networkSettings)
    }

    @Provides
    @Singleton
    open fun provideConnectedDevicesService(devicesDiscoverer: IDevicesDiscoverer, messenger: IMessenger, syncManager: ISyncManager, registrationHandler: IDeviceRegistrationHandler,
                                            networkSettings: NetworkSettings, entityManager: IEntityManager) : IDiscoveredDevicesManager {
        return DiscoveredDevicesManager(devicesDiscoverer, messenger, syncManager, registrationHandler, networkSettings, entityManager,
                ConnectedDevicesServiceConfig.DEVICES_DISCOVERER_PORT, ConnectedDevicesServiceConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS)
    }

    @Provides
    @Singleton
    open fun provideCommunicationManager(discoveredDevicesManager: IDiscoveredDevicesManager, syncManager: ISyncManager, messenger: IMessenger, networkSettings: NetworkSettings)
            : ICommunicationManager {
        return CommunicationManager(discoveredDevicesManager, syncManager, messenger, networkSettings)
    }

}
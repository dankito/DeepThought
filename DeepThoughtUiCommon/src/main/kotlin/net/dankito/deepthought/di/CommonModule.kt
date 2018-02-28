package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.synchronization.device.messaging.tcp.TcpSocketClientCommunicator
import net.dankito.deepthought.communication.CommunicationManager
import net.dankito.deepthought.communication.ICommunicationManager
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.MimeTypeService
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContentDetector
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.feedaddressextractor.FeedAddressExtractor
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
import net.dankito.service.synchronization.*
import net.dankito.service.synchronization.changeshandler.ISynchronizedChangesHandler
import net.dankito.service.synchronization.changeshandler.SynchronizedChangesHandler
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.util.IThreadPool
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
    open fun provideOptionsForClipboardContentDetector(articleExtractorManager: ArticleExtractorManager, fileManager: FileManager, dialogService: IDialogService,
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
    fun provideMimeTypeService(mimeTypeDetector: MimeTypeDetector, mimeTypeCategorizer: MimeTypeCategorizer, dataManager: DataManager) : MimeTypeService {
        return MimeTypeService(mimeTypeDetector, mimeTypeCategorizer, dataManager)
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
    open fun provideSearchEngine(dataManager: DataManager, languageDetector: ILanguageDetector, threadPool: IThreadPool, osHelper: OsHelper, eventBus: IEventBus,
                                 itemService: ItemService, tagService: TagService, sourceService: SourceService, seriesService: SeriesService,
                                 readLaterArticleService: ReadLaterArticleService, fileService: FileService, localFileInfoService: LocalFileInfoService) : ISearchEngine {
        return LuceneSearchEngine(dataManager, languageDetector, osHelper, threadPool, eventBus, itemService, tagService, sourceService, seriesService,
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
    open fun provideClientCommunicator(networkSettings: NetworkSettings, registrationHandler: IDeviceRegistrationHandler, entityManager: IEntityManager,
                                       serializer: ISerializer, base64Service: IBase64Service, hashService: HashService, threadPool: IThreadPool) : IMessenger {
        return TcpSocketClientCommunicator(networkSettings, registrationHandler, entityManager, serializer, base64Service, hashService, threadPool)
    }

    @Provides
    @Singleton
    open fun provideInitialSyncManager(entityManager: IEntityManager, localization: Localization) : InitialSyncManager {
        return InitialSyncManager(entityManager, localization)
    }

    @Provides
    @Singleton
    open fun provideSynchronizedChangesHandler(entityManager: IEntityManager, changesNotifier: EntityChangedNotifier) : ISynchronizedChangesHandler {
        return SynchronizedChangesHandler(entityManager as CouchbaseLiteEntityManagerBase, changesNotifier)
    }

    @Provides
    @Singleton
    open fun provideSyncManager(entityManager: IEntityManager, changesHandler: ISynchronizedChangesHandler, networkSettings: NetworkSettings) : ISyncManager {
        return CouchbaseLiteSyncManager(entityManager as CouchbaseLiteEntityManagerBase, changesHandler, networkSettings)
    }

    @Provides
    @Singleton
    open fun provideConnectedDevicesService(devicesDiscoverer: IDevicesDiscoverer, messenger: IMessenger, syncManager: ISyncManager, registrationHandler: IDeviceRegistrationHandler,
                                            networkSettings: NetworkSettings, entityManager: IEntityManager) : IConnectedDevicesService {
        return ConnectedDevicesService(devicesDiscoverer, messenger, syncManager, registrationHandler, networkSettings, entityManager)
    }

    @Provides
    @Singleton
    open fun provideCommunicationManager(connectedDevicesService: IConnectedDevicesService, syncManager: ISyncManager, messenger: IMessenger, networkSettings: NetworkSettings)
            : ICommunicationManager {
        return CommunicationManager(connectedDevicesService, syncManager, messenger, networkSettings)
    }

}
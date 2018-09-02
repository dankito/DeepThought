package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.CouchbaseLiteEntityManagerBase
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.TcpSocketClientCommunicator
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import net.dankito.deepthought.communication.CommunicationManager
import net.dankito.deepthought.communication.ICommunicationManager
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.MimeTypeService
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.model.NetworkSettings
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
import net.dankito.service.synchronization.ConnectedDevicesService
import net.dankito.service.synchronization.CouchbaseLiteSyncManager
import net.dankito.service.synchronization.IConnectedDevicesService
import net.dankito.service.synchronization.ISyncManager
import net.dankito.service.synchronization.changeshandler.ISynchronizedChangesHandler
import net.dankito.service.synchronization.changeshandler.SynchronizedChangesHandler
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.ImageCache
import net.dankito.utils.OsHelper
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.localization.Localization
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.hashing.HashService
import net.dankito.utils.hashing.IBase64Service
import net.dankito.utils.services.network.NetworkHelper
import net.dankito.utils.ui.dialogs.IDialogService
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
    open fun provideNetworkSettings(dataManager: DataManager) : INetworkSettings {
        return NetworkSettings(dataManager.localDevice, dataManager.localUser)
    }

    @Provides
    @Singleton
    open fun provideClientCommunicator(networkSettings: INetworkSettings, registrationHandler: IDeviceRegistrationHandler, entityManager: IEntityManager,
                                  serializer: ISerializer, base64Service: IBase64Service, hashService: HashService, threadPool: IThreadPool) : IClientCommunicator {
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
    open fun provideSyncManager(entityManager: IEntityManager, changesHandler: ISynchronizedChangesHandler, networkSettings: INetworkSettings) : ISyncManager {
        return CouchbaseLiteSyncManager(entityManager as CouchbaseLiteEntityManagerBase, changesHandler, networkSettings)
    }

    @Provides
    @Singleton
    open fun provideConnectedDevicesService(devicesDiscoverer: IDevicesDiscoverer, clientCommunicator: IClientCommunicator, syncManager: ISyncManager, registrationHandler: IDeviceRegistrationHandler,
                                       networkSettings: INetworkSettings, entityManager: IEntityManager) : IConnectedDevicesService {
        return ConnectedDevicesService(devicesDiscoverer, clientCommunicator, syncManager, registrationHandler, networkSettings, entityManager)
    }

    @Provides
    @Singleton
    open fun provideCommunicationManager(connectedDevicesService: IConnectedDevicesService, syncManager: ISyncManager, clientCommunicator: IClientCommunicator, networkSettings: INetworkSettings)
            : ICommunicationManager {
        return CommunicationManager(connectedDevicesService, syncManager, clientCommunicator, networkSettings)
    }

}
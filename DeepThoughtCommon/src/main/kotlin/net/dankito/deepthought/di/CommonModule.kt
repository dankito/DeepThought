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
import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.deepthought.communication.CommunicationManager
import net.dankito.deepthought.communication.ICommunicationManager
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.model.NetworkSettings
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.data.DataManager
import net.dankito.faviconextractor.FaviconComparator
import net.dankito.faviconextractor.FaviconExtractor
import net.dankito.feedaddressextractor.FeedAddressExtractor
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.feed.IFeedReader
import net.dankito.newsreader.feed.RomeFeedReader
import net.dankito.serializer.ISerializer
import net.dankito.serializer.JacksonJsonSerializer
import net.dankito.service.data.ArticleSummaryExtractorConfigService
import net.dankito.service.data.EntryService
import net.dankito.service.data.TagService
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.LuceneSearchEngine
import net.dankito.service.synchronization.ConnectedDevicesService
import net.dankito.service.synchronization.CouchbaseLiteSyncManager
import net.dankito.service.synchronization.ISyncManager
import net.dankito.service.synchronization.changeshandler.SynchronizedChangesHandler
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.IThreadPool
import net.dankito.utils.ImageCache
import net.dankito.utils.ThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.services.hashing.IBase64Service
import javax.inject.Singleton


@Module
class CommonModule {

    @Provides
    @Singleton
    fun provideLocalization() : Localization {
        return Localization()
    }


    @Provides
    @Singleton
    fun provideWebClient() : IWebClient {
        return OkHttpWebClient()
    }

    @Provides
    @Singleton
    fun provideSerializer() : ISerializer {
        return JacksonJsonSerializer()
    }


    @Provides
    @Singleton
    fun provideThreadPool() : IThreadPool {
        return ThreadPool()
    }

    @Provides
    @Singleton
    fun provideImageCache(webClient: IWebClient, serializer: ISerializer, fileStorageService: IFileStorageService) : ImageCache {
        return ImageCache(webClient,serializer, fileStorageService)
    }


    @Provides
    @Singleton
    fun provideSearchEngine(dataManager: DataManager, threadPool: IThreadPool, eventBus: IEventBus, entryService: EntryService, tagService: TagService) : ISearchEngine {
        return LuceneSearchEngine(dataManager, threadPool, eventBus, entryService, tagService)
    }


    @Provides
    @Singleton
    fun provideFaviconExtractor(webClient: IWebClient) : FaviconExtractor {
        return FaviconExtractor(webClient)
    }

    @Provides
    @Singleton
    fun provideFaviconComparator(webClient: IWebClient) : FaviconComparator {
        return FaviconComparator(webClient)
    }


    @Provides
    @Singleton
    fun provideArticleSummaryExtractorConfigManager(webClient: IWebClient, configService: ArticleSummaryExtractorConfigService, threadPool: IThreadPool) : ArticleSummaryExtractorConfigManager {
        return ArticleSummaryExtractorConfigManager(webClient, configService, threadPool)
    }

    @Provides
    @Singleton
    fun provideArticleExtractors(webClient: IWebClient) : ArticleExtractors {
        return ArticleExtractors(webClient)
    }


    @Provides
    @Singleton
    fun provideFeedAddressExtractor(webClient: IWebClient) : FeedAddressExtractor {
        return FeedAddressExtractor(webClient)
    }

    @Provides
    @Singleton
    fun provideFeedReader() : IFeedReader {
        return RomeFeedReader()
    }


    @Provides
    @Singleton
    fun provideNetworkSettings(dataManager: DataManager) : INetworkSettings {
        return NetworkSettings(dataManager.localDevice, dataManager.localUser)
    }

    @Provides
    @Singleton
    fun provideClientCommunicator(networkSettings: INetworkSettings, registrationHandler: IDeviceRegistrationHandler, base64Service: IBase64Service, threadPool: IThreadPool)
            : IClientCommunicator {
        return TcpSocketClientCommunicator(networkSettings, registrationHandler, base64Service, threadPool)
    }

    @Provides
    @Singleton
    fun provideInitialSyncManager(entityManager: IEntityManager, localization: Localization) : InitialSyncManager {
        return InitialSyncManager(entityManager, localization)
    }

    @Provides
    @Singleton
    fun provideSynchronizedChangesHandler(entityManager: IEntityManager, changesNotifier: EntityChangedNotifier) : SynchronizedChangesHandler {
        return SynchronizedChangesHandler(entityManager, changesNotifier)
    }

    @Provides
    @Singleton
    fun provideSyncManager(entityManager: IEntityManager, changesHandler: SynchronizedChangesHandler, networkSettings: INetworkSettings) : ISyncManager {
        return CouchbaseLiteSyncManager(entityManager as CouchbaseLiteEntityManagerBase, changesHandler, networkSettings)
    }

    @Provides
    @Singleton
    fun provideConnectedDevicesService(devicesDiscoverer: IDevicesDiscoverer, clientCommunicator: IClientCommunicator, syncManager: ISyncManager, registrationHandler: IDeviceRegistrationHandler,
                                       networkSettings: INetworkSettings, entityManager: IEntityManager) : ConnectedDevicesService {
        return ConnectedDevicesService(devicesDiscoverer, clientCommunicator, syncManager, registrationHandler, networkSettings, entityManager)
    }

    @Provides
    @Singleton
    fun provideCommunicationManager(connectedDevicesService: ConnectedDevicesService, syncManager: ISyncManager, clientCommunicator: IClientCommunicator, networkSettings: INetworkSettings)
            : ICommunicationManager {
        return CommunicationManager(connectedDevicesService, syncManager, clientCommunicator, networkSettings)
    }

}
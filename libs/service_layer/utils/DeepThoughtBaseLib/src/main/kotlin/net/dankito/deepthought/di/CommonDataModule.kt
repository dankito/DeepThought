package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.network.communication.SocketHandler
import net.dankito.deepthought.data.FilePersister
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.files.FileManager
import net.dankito.deepthought.files.synchronization.FileServer
import net.dankito.deepthought.files.synchronization.FileSyncService
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.deepthought.service.permissions.IPermissionsService
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.synchronization.IConnectedDevicesService
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.services.hashing.HashService
import net.dankito.utils.settings.ILocalSettingsStore
import net.dankito.utils.ui.IDialogService
import javax.inject.Singleton


@Module
class CommonDataModule {

    @Provides
    @Singleton
    fun provideDefaultDataInitializer(platformConfiguration: IPlatformConfiguration, localization: Localization) : DefaultDataInitializer {
        return DefaultDataInitializer(platformConfiguration, localization)
    }

    @Provides
    @Singleton
    fun provideDataManager(entityManager: IEntityManager, configuration: EntityManagerConfiguration,
                           dataInitializer: DefaultDataInitializer, platformConfiguration: IPlatformConfiguration) : DataManager {
        return DataManager(entityManager, configuration, dataInitializer, platformConfiguration)
    }

    @Provides
    @Singleton
    fun provideEntityChangedNotifier(eventBus: IEventBus) : EntityChangedNotifier {
        return EntityChangedNotifier(eventBus)
    }


    @Provides
    @Singleton
    fun provideItemService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : ItemService {
        return ItemService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideReferenceService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : SourceService {
        return SourceService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideSeriesService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : SeriesService {
        return SeriesService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideTagService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : TagService {
        return TagService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideReadLaterArticleService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier, serializer: ISerializer) : ReadLaterArticleService {
        return ReadLaterArticleService(dataManager, entityChangedNotifier, serializer)
    }

    @Provides
    @Singleton
    fun provideLocalFileInfoService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier): LocalFileInfoService {
        return LocalFileInfoService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideFileService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier): FileService {
        return FileService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideFileManager(searchEngine: ISearchEngine, localFileInfoService: LocalFileInfoService, fileSyncService: FileSyncService, platformConfiguration: IPlatformConfiguration,
                           hashService: HashService, eventBus: IEventBus, threadPool: IThreadPool) : FileManager {
        return FileManager(searchEngine, localFileInfoService, fileSyncService, platformConfiguration, hashService, eventBus, threadPool)
    }

    @Provides
    @Singleton
    fun provideArticleSummaryExtractorConfigService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : ArticleSummaryExtractorConfigService {
        return ArticleSummaryExtractorConfigService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideDeleteEntityService(itemService: ItemService, tagService: TagService, sourceService: SourceService, seriesService: SeriesService,
                                   fileService: FileService, localFileInfoService: LocalFileInfoService, searchEngine: ISearchEngine, dialogService: IDialogService, threadPool: IThreadPool) : DeleteEntityService {
        return DeleteEntityService(itemService, tagService, sourceService, seriesService, fileService, localFileInfoService, searchEngine, dialogService, threadPool)
    }

    @Provides
    @Singleton
    fun provideSocketHandler(): SocketHandler {
        return SocketHandler()
    }

    @Provides
    @Singleton
    fun provideFileSyncService(connectedDevicesService: IConnectedDevicesService, searchEngine: ISearchEngine, socketHandler: SocketHandler, localFileInfoService: LocalFileInfoService,
                               serializer: ISerializer, permissionsService: IPermissionsService, platformConfiguration: IPlatformConfiguration, hashService: HashService): FileSyncService {
        return FileSyncService(connectedDevicesService, searchEngine, socketHandler, localFileInfoService, serializer, permissionsService, platformConfiguration, hashService)
    }

    @Provides
    @Singleton
    fun provideFileServer(searchEngine: ISearchEngine, entityManager: IEntityManager, networkSettings: INetworkSettings,
                          socketHandler: SocketHandler, serializer: ISerializer, threadPool: IThreadPool): FileServer {
        return FileServer(searchEngine, entityManager, networkSettings, socketHandler, serializer, threadPool)
    }


    @Provides
    @Singleton
    fun provideSerializer(tagService: TagService, seriesService: SeriesService) : ISerializer {
        return JacksonJsonSerializer(tagService, seriesService)
    }

    @Provides
    @Singleton
    fun provideItemPersister(itemService: ItemService, referencePersister: ReferencePersister, tagService: TagService, filePersister: FilePersister,
                              deleteEntityService: DeleteEntityService): ItemPersister {
        return ItemPersister(itemService, referencePersister, tagService, filePersister, deleteEntityService)
    }

    @Provides
    @Singleton
    fun provideReferencePersister(sourceService: SourceService, seriesService: SeriesService, filePersister: FilePersister, deleteEntityService: DeleteEntityService)
            : ReferencePersister {
        return ReferencePersister(sourceService, seriesService, filePersister, deleteEntityService)
    }

    @Provides
    @Singleton
    fun provideSeriesPersister(seriesService: SeriesService): SeriesPersister {
        return SeriesPersister(seriesService)
    }

    @Provides
    @Singleton
    fun provideFilePersister(fileService: FileService, localFileInfoService: LocalFileInfoService, fileManager: FileManager, threadPool: IThreadPool): FilePersister {
        return FilePersister(fileService, localFileInfoService, fileManager, threadPool)
    }

    @Provides
    @Singleton
    fun provideHashService() : HashService {
        return HashService()
    }


    @Provides
    @Singleton
    fun provideEntityManagerConfiguration(localSettingsStore: ILocalSettingsStore) : EntityManagerConfiguration {
        return EntityManagerConfiguration(localSettingsStore.getDataFolder(), "deep_thought_db")
    }

}
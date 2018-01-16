package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.data.FileManager
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.serialization.JacksonJsonSerializer
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
    fun provideEntryService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : EntryService {
        return EntryService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideReferenceService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : ReferenceService {
        return ReferenceService(dataManager, entityChangedNotifier)
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
    fun provideFileService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier): FileService {
        return FileService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideFileManager(platformConfiguration: IPlatformConfiguration) : FileManager {
        return FileManager(platformConfiguration)
    }

    @Provides
    @Singleton
    fun provideArticleSummaryExtractorConfigService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : ArticleSummaryExtractorConfigService {
        return ArticleSummaryExtractorConfigService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideDeleteEntityService(entryService: EntryService, tagService: TagService, referenceService: ReferenceService, seriesService: SeriesService,
                                   dialogService: IDialogService, threadPool: IThreadPool) : DeleteEntityService {
        return DeleteEntityService(entryService, tagService, referenceService, seriesService, dialogService, threadPool)
    }


    @Provides
    @Singleton
    fun provideSerializer(tagService: TagService, seriesService: SeriesService) : ISerializer {
        return JacksonJsonSerializer(tagService, seriesService)
    }

    @Provides
    @Singleton
    fun provideEntryPersister(entryService: EntryService, referencePersister: ReferencePersister, tagService: TagService): EntryPersister {
        return EntryPersister(entryService, referencePersister, tagService)
    }

    @Provides
    @Singleton
    fun provideReferencePersister(referenceService: ReferenceService, seriesService: SeriesService): ReferencePersister {
        return ReferencePersister(referenceService, seriesService)
    }

    @Provides
    @Singleton
    fun provideSeriesPersister(seriesService: SeriesService): SeriesPersister {
        return SeriesPersister(seriesService)
    }


    @Provides
    @Singleton
    fun provideEntityManagerConfiguration(localSettingsStore: ILocalSettingsStore) : EntityManagerConfiguration {
        return EntityManagerConfiguration(localSettingsStore.getDataFolder(), "deep_thought_db")
    }

}
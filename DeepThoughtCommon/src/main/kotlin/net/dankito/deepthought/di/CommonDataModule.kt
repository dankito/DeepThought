package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.enums.ApplicationLanguage
import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.model.enums.NoteType
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.service.data.DefaultDataInitializer
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.serializer.ISerializer
import net.dankito.service.data.*
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.localization.Localization
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
    fun provideArticleSummaryExtractorConfigService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier) : ArticleSummaryExtractorConfigService {
        return ArticleSummaryExtractorConfigService(dataManager, entityChangedNotifier)
    }

    @Provides
    @Singleton
    fun provideDeleteEntityService(entryService: EntryService, tagService: TagService, referenceService: ReferenceService, threadPool: IThreadPool) : DeleteEntityService {
        return DeleteEntityService(entryService, tagService, referenceService, threadPool)
    }


    @Provides
    @Singleton
    fun provideEntryPersister(entryService: EntryService, referenceService: ReferenceService, tagService: TagService): EntryPersister {
        return EntryPersister(entryService, referenceService, tagService)
    }


    @Provides
    @Singleton
    fun provideEntityManagerConfiguration() : EntityManagerConfiguration {
        val configuration = EntityManagerConfiguration("data", "deep_thought_db")

        configuration.entityClasses = listOf<Class<*>>(
                DeepThought::class.java,

                ApplicationLanguage::class.java,
                FileType::class.java,
                NoteType::class.java,


                User::class.java,
                Device::class.java,


                Entry::class.java,
                Tag::class.java,
                Reference::class.java,
                FileLink::class.java,
                Note::class.java,
                EntriesGroup::class.java
        )

        return configuration
    }

}
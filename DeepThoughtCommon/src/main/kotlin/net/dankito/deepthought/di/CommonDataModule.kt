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
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IPlatformConfiguration
import javax.inject.Singleton


@Module
class CommonDataModule {

    @Provides
    @Singleton
    fun provideDefaultDataInitializer(platformConfiguration: IPlatformConfiguration) : DefaultDataInitializer {
        return DefaultDataInitializer(platformConfiguration)
    }

    @Provides
    @Singleton
    fun provideDataManager(entityManager: IEntityManager, configuration: EntityManagerConfiguration,
                           dataInitializer: DefaultDataInitializer, platformConfiguration: IPlatformConfiguration) : DataManager {
        return DataManager(entityManager, configuration, dataInitializer, platformConfiguration)
    }


    @Provides
    @Singleton
    fun provideEntryService(dataManager: DataManager, eventBus: IEventBus) : EntryService {
        return EntryService(dataManager, eventBus)
    }

    @Provides
    @Singleton
    fun provideReferenceService(dataManager: DataManager, eventBus: IEventBus) : ReferenceService {
        return ReferenceService(dataManager, eventBus)
    }

    @Provides
    @Singleton
    fun provideTagService(dataManager: DataManager, eventBus: IEventBus) : TagService {
        return TagService(dataManager, eventBus)
    }


    @Provides
    @Singleton
    fun provideEntityManagerConfiguration() : EntityManagerConfiguration {
        val configuration = EntityManagerConfiguration("data", "deep_thought_db")

        configuration.entityClasses = listOf<Class<*>>(
                DeepThoughtApplication::class.java,

                ApplicationLanguage::class.java,

                User::class.java,
                Device::class.java,
                UsersGroup::class.java,


                DeepThought::class.java,

                FileType::class.java,
                NoteType::class.java,

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
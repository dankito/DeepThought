package net.dankito.deepthought.android.service.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.AndroidCouchbaseLiteEntityManager
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.filesystem.AndroidFileStorageService
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.deepthought.android.service.platform.AndroidPlatformConfiguration
import net.dankito.utils.IPlatformConfiguration
import javax.inject.Singleton


@Module
class AndroidCommonModule {

    @Provides
    @Singleton
    fun providePlatformConfiguration(context: Context) : IPlatformConfiguration {
        return AndroidPlatformConfiguration(context)
    }


    @Provides
    @Singleton
    fun provideEntityManager(context: Context, configuration: EntityManagerConfiguration) : IEntityManager {
        val entityManager = AndroidCouchbaseLiteEntityManager(context)

        entityManager.open(configuration)

        return entityManager
    }

    @Provides
    @Singleton
    fun provideFileStorageService(context: Context) : IFileStorageService {
        return AndroidFileStorageService(context)
    }

}
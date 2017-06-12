package net.dankito.deepthought.java.service.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.filesystem.JavaFileStorageService
import javax.inject.Singleton


@Module
class JavaCommonModule {

    @Provides
    @Singleton
    fun provideEntityManager(configuration: EntityManagerConfiguration) : IEntityManager {
        val entityManager = JavaCouchbaseLiteEntityManager(configuration)

        entityManager.open(configuration)

        return entityManager
    }

    @Provides
    @Singleton
    fun provideFileStorageService() : IFileStorageService {
        return JavaFileStorageService()
    }

}
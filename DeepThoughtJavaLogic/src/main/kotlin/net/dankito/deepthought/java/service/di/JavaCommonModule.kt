package net.dankito.deepthought.java.service.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.data_access.network.discovery.UdpDevicesDiscoverer
import net.dankito.deepthought.java.service.hashing.Java8Base64Service
import net.dankito.deepthought.java.service.platform.JavaPlatformConfiguration
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.hashing.IBase64Service
import net.dankito.utils.services.network.INetworkConnectivityManager
import javax.inject.Singleton


@Module
class JavaCommonModule {

    @Provides
    @Singleton
    fun providePlatformConfiguration() : IPlatformConfiguration {
        return JavaPlatformConfiguration()
    }


    @Provides
    @Singleton
    fun provideEntityManager(configuration: EntityManagerConfiguration) : IEntityManager {
        return JavaCouchbaseLiteEntityManager(configuration)
    }

    @Provides
    @Singleton
    fun provideDevicesDiscoverer(networkConnectivityManager: INetworkConnectivityManager, threadPool: IThreadPool) : IDevicesDiscoverer {
        return UdpDevicesDiscoverer(networkConnectivityManager, threadPool)
    }

    @Provides
    @Singleton
    fun provideFileStorageService() : IFileStorageService {
        return JavaFileStorageService()
    }


    @Provides
    @Singleton
    fun provideBase64Service() : IBase64Service {
        return Java8Base64Service()
    }

}
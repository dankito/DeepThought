package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.filesystem.JavaFileStorageService
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.data_access.network.discovery.UdpDevicesDiscoverer
import net.dankito.deepthought.javafx.service.hashing.Java8Base64Service
import net.dankito.deepthought.javafx.service.settings.JavaFXPlatformConfiguration
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.utils.IPlatformConfiguration
import net.dankito.util.IThreadPool
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.settings.ILocalSettingsStore
import javax.inject.Singleton


@Module
class JavaCommonModule {

    @Provides
    @Singleton
    fun providePlatformConfiguration() : IPlatformConfiguration {
        return JavaFXPlatformConfiguration()
    }


    @Provides
    @Singleton
    fun provideEntityManager(configuration: EntityManagerConfiguration, localSettingsStore: ILocalSettingsStore) : IEntityManager {
        return JavaCouchbaseLiteEntityManager(configuration, localSettingsStore)
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
    fun providePdfImporter(threadPool: IThreadPool) : PdfImporter {
        return PdfImporter(threadPool)
    }


    @Provides
    @Singleton
    fun provideBase64Service() : IBase64Service {
        return Java8Base64Service()
    }

}
package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.JavaCouchbaseLiteEntityManager
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.data_access.network.discovery.UdpDevicesDiscoverer
import net.dankito.utils.javafx.util.hashing.Java8Base64Service
import net.dankito.deepthought.javafx.service.settings.JavaFXPlatformConfiguration
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.hashing.IBase64Service
import net.dankito.utils.io.IFileStorageService
import net.dankito.utils.io.JavaFileStorageService
import net.dankito.utils.services.network.INetworkConnectivityManager
import net.dankito.utils.settings.ILocalSettingsStore
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
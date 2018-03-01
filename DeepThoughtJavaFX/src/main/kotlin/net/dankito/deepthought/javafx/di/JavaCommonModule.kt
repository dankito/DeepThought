package net.dankito.deepthought.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.javafx.service.hashing.Java8Base64Service
import net.dankito.deepthought.javafx.service.settings.JavaFXPlatformConfiguration
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.deepthought.database.CouchbaseLiteDatabaseUtil
import net.dankito.jpa.couchbaselite.CouchbaseLiteEntityManagerBase
import net.dankito.jpa.couchbaselite.JavaCouchbaseLiteEntityManager
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
import net.dankito.synchronization.device.discovery.udp.UdpDevicesDiscoverer
import net.dankito.util.IThreadPool
import net.dankito.util.filesystem.IFileStorageService
import net.dankito.util.filesystem.JavaFileStorageService
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.settings.ILocalSettingsStore
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.database.IDatabaseUtil
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
    fun provideDatabaseUtil(entityManager: IEntityManager) : IDatabaseUtil {
        return CouchbaseLiteDatabaseUtil(entityManager as CouchbaseLiteEntityManagerBase)
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
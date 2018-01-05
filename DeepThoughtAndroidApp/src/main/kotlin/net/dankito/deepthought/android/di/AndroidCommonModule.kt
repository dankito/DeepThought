package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.data_access.database.AndroidCouchbaseLiteEntityManager
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.filesystem.AndroidFileStorageService
import net.dankito.data_access.filesystem.IFileStorageService
import net.dankito.data_access.network.discovery.AndroidUdpDevicesDiscoverer
import net.dankito.data_access.network.discovery.IDevicesDiscoverer
import net.dankito.deepthought.android.service.hashing.AndroidBase64Service
import net.dankito.deepthought.android.service.settings.AndroidPlatformConfiguration
import net.dankito.deepthought.service.importexport.pdf.PdfImporter
import net.dankito.utils.IPlatformConfiguration
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.hashing.IBase64Service
import net.dankito.utils.services.network.INetworkConnectivityManager
import net.dankito.utils.settings.ILocalSettingsStore
import javax.inject.Singleton


@Module
open class AndroidCommonModule {

    @Provides
    @Singleton
    open fun providePlatformConfiguration(context: Context) : IPlatformConfiguration {
        return AndroidPlatformConfiguration(context)
    }


    @Provides
    @Singleton
    open fun provideEntityManager(context: Context, configuration: EntityManagerConfiguration, localSettingsStore: ILocalSettingsStore) : IEntityManager {
        return AndroidCouchbaseLiteEntityManager(context, localSettingsStore)
    }

    @Provides
    @Singleton
    open fun provideDevicesDiscoverer(context: Context, networkConnectivityManager: INetworkConnectivityManager, threadPool: IThreadPool) : IDevicesDiscoverer {
        return AndroidUdpDevicesDiscoverer(context, networkConnectivityManager, threadPool)
    }

    @Provides
    @Singleton
    open fun provideFileStorageService(context: Context) : IFileStorageService {
        return AndroidFileStorageService(context)
    }


    @Provides
    @Singleton
    open fun providePdfImporter(applicationContext: Context, threadPool: IThreadPool) : PdfImporter {
        return PdfImporter(applicationContext, threadPool)
    }


    @Provides
    @Singleton
    open fun provideBase64Service() : IBase64Service {
        return AndroidBase64Service()
    }

}
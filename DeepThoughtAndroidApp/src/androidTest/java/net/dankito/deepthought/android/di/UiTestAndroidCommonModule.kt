package net.dankito.deepthought.android.di

import android.content.Context
import net.dankito.data_access.database.EntityManagerConfiguration
import net.dankito.data_access.database.IEntityManager
import net.dankito.data_access.database.InMemoryEntityManager
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
import net.dankito.synchronization.device.discovery.NoOpDevicesDiscoverer
import net.dankito.util.IThreadPool
import net.dankito.util.network.INetworkConnectivityManager
import net.dankito.util.settings.ILocalSettingsStore


class UiTestAndroidCommonModule : AndroidCommonModule() {

    override fun provideEntityManager(context: Context, configuration: EntityManagerConfiguration, localSettingsStore: ILocalSettingsStore): IEntityManager {
        return InMemoryEntityManager()
    }

    override fun provideDevicesDiscoverer(context: Context, networkConnectivityManager: INetworkConnectivityManager, threadPool: IThreadPool): IDevicesDiscoverer {
        return NoOpDevicesDiscoverer()
    }

}
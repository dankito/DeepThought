package net.dankito.deepthought.android.di

import android.content.Context
import net.dankito.deepthought.android.stub.InMemoryEntityManager
import net.dankito.deepthought.android.stub.NoOpDevicesDiscoverer
import net.dankito.synchronization.database.EntityManagerConfiguration
import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.device.discovery.IDevicesDiscoverer
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
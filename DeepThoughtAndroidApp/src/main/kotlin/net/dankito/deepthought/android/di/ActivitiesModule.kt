package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.appstart.AndroidAppInitializer
import net.dankito.deepthought.android.appstart.CommunicationManagerStarter
import net.dankito.deepthought.android.dialogs.AndroidDialogService
import net.dankito.deepthought.android.routing.AndroidRouter
import net.dankito.deepthought.android.service.AndroidClipboardService
import net.dankito.deepthought.android.service.communication.AndroidDeviceRegistrationHandler
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.serializer.ISerializer
import net.dankito.service.search.ISearchEngine
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import javax.inject.Singleton


@Module
class ActivitiesModule(private val application: DeepThoughtApplication) {

    @Provides
    @Singleton
    fun provideApplicationContext() : Context {
        return application
    }


    @Provides
    @Singleton
    fun provideAppInitializer() : AndroidAppInitializer {
        return AndroidAppInitializer()
    }


    @Provides
    @Singleton
    fun provideCurrentActivityTracker() : CurrentActivityTracker {
        return CurrentActivityTracker()
    }

    @Provides
    @Singleton
    fun provideClipboardService(activityTracker: CurrentActivityTracker) : IClipboardService {
        return AndroidClipboardService(activityTracker)
    }

    @Provides
    @Singleton
    fun provideDialogService(currentActivityTracker: CurrentActivityTracker) : IDialogService {
        return AndroidDialogService(currentActivityTracker)
    }


    @Provides
    @Singleton
    fun provideRouter(context: Context, activityTracker: CurrentActivityTracker, serializer: ISerializer) : IRouter {
        return AndroidRouter(context, activityTracker, serializer)
    }


    @Provides
    @Singleton
    fun provideDeviceRegistrationHandler(context: Context, dataManager: DataManager, initialSyncManager: InitialSyncManager, dialogService: IDialogService,
                                           localization: Localization, currentActivityTracker: CurrentActivityTracker) : IDeviceRegistrationHandler {
        return AndroidDeviceRegistrationHandler(context, dataManager, initialSyncManager, dialogService, localization, currentActivityTracker)
    }

    @Provides
    @Singleton
    fun provideCommunicationManagerStarter(searchEngine: ISearchEngine) : CommunicationManagerStarter {
        return CommunicationManagerStarter(searchEngine)
    }

}
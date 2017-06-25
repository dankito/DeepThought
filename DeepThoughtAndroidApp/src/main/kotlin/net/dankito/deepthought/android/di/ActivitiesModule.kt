package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.dialogs.AndroidDialogService
import net.dankito.deepthought.android.routing.AndroidRouter
import net.dankito.deepthought.android.service.communication.AndroidIsSynchronizationPermittedHandler
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.ui.IDialogService
import net.dankito.deepthought.ui.IRouter
import net.dankito.serializer.ISerializer
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
    fun provideCurrentActivityTracker() : CurrentActivityTracker {
        return CurrentActivityTracker()
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
    fun provideIsSynchronizationPermittedHandler(context: Context) : IsSynchronizationPermittedHandler {
        return AndroidIsSynchronizationPermittedHandler(context)
    }

}
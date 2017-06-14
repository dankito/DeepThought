package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.routing.AndroidRouter
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
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
    fun provideRouter(context: Context, activityTracker: CurrentActivityTracker, serializer: ISerializer) : IRouter {
        return AndroidRouter(context, activityTracker, serializer)
    }

}
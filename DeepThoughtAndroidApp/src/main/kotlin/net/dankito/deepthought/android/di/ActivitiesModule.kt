package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.routing.Router
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
    fun provideRouter(context: Context) : Router {
        return Router(context)
    }

}
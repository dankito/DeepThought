package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.deepthought.android.DeepThoughtApplication
import net.dankito.deepthought.android.routing.Router
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
    fun provideRouter(context: Context, serializer: ISerializer) : Router {
        return Router(context, serializer)
    }

}
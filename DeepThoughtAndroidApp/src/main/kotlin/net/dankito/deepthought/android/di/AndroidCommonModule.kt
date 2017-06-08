package net.dankito.deepthought.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import net.dankito.data_access.filesystem.AndroidFileStorageService
import net.dankito.data_access.filesystem.IFileStorageService
import javax.inject.Singleton


@Module
class AndroidCommonModule {


    @Provides
    @Singleton
    fun provideFileStorageService(context: Context) : IFileStorageService {
        return AndroidFileStorageService(context)
    }

}
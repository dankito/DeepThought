package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.service.data.DataManager
import net.dankito.mime.MimeTypeCategorizer
import net.dankito.mime.MimeTypeDetector
import net.dankito.mime.MimeTypePicker
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.language.NoOpLanguageDetector
import net.dankito.utils.language.SupportedLanguages
import net.dankito.utils.network.download.IFileDownloader
import net.dankito.utils.services.network.download.WGetFileDownloader
import net.dankito.utils.web.UrlUtil
import javax.inject.Singleton


@Module
class BaseModule {


    @Provides
    @Singleton
    fun provideEventBus() : IEventBus {
        return MBassadorEventBus()
    }

    @Provides
    @Singleton
    fun provideThreadPool() : IThreadPool {
        return ThreadPool()
    }

    @Provides
    @Singleton
    fun provideFileDownloader(threadPool: IThreadPool) : IFileDownloader {
        return WGetFileDownloader(threadPool)
    }

    @Provides
    @Singleton
    fun provideUrlUtil() : UrlUtil {
        return UrlUtil()
    }

    @Provides
    @Singleton
    fun provideMimeTypePicker() : MimeTypePicker {
        return MimeTypePicker()
    }

    @Provides
    @Singleton
    fun provideMimeTypeDetector(mimeTypePicker: MimeTypePicker) : MimeTypeDetector {
        return MimeTypeDetector(mimeTypePicker)
    }

    @Provides
    @Singleton
    fun provideMimeTypeCategorizer() : MimeTypeCategorizer {
        return MimeTypeCategorizer()
    }

    @Provides
    @Singleton
    fun provideSupportedLanguages() : SupportedLanguages {
        return SupportedLanguages()
    }

    @Provides
    @Singleton
    fun provideLanguageDetector(dataManager: DataManager, supportedLanguages: SupportedLanguages) : ILanguageDetector {
        return NoOpLanguageDetector()
    }


}
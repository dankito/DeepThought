package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.utils.UrlUtil
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.language.NoOpLanguageDetector
import net.dankito.utils.language.SupportedLanguages
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
    fun provideUrlUtil() : UrlUtil {
        return UrlUtil()
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
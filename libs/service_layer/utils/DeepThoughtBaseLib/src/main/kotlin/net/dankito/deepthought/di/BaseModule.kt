package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import net.dankito.utils.language.ILanguageDetector
import net.dankito.utils.language.NorconexLanguageDetector
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
    fun provideSupportedLanguages() : SupportedLanguages {
        return SupportedLanguages()
    }

    @Provides
    @Singleton
    fun provideLanguageDetector(supportedLanguages: SupportedLanguages) : ILanguageDetector {
        return NorconexLanguageDetector(supportedLanguages)
    }


}
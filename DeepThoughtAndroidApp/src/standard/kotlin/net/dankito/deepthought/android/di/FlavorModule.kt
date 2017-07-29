package net.dankito.deepthought.android.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import net.dankito.deepthought.android.service.reporting.NoOpCrashReporter
import javax.inject.Singleton


@Module
class FlavorModule {

    @Provides
    @Singleton
    fun provideCrashReporter() : ICrashReporter {
        return NoOpCrashReporter()
    }

}
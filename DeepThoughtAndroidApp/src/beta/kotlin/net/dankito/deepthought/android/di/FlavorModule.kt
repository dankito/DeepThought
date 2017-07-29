package net.dankito.deepthought.android.di

import dagger.Module
import dagger.Provides
import net.dankito.deepthought.android.reporting.FabricCrashReporter
import net.dankito.deepthought.android.reporting.TestFairyCrashReporter
import net.dankito.deepthought.android.service.reporting.ICrashReporter
import javax.inject.Singleton


@Module
class FlavorModule {

    @Provides
    @Singleton
    fun provideCrashReporter() : ICrashReporter {
        return TestFairyCrashReporter()
    }

}
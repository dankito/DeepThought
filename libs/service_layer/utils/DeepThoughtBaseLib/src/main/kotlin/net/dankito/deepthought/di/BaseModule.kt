package net.dankito.deepthought.di

import dagger.Module
import dagger.Provides
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.eventbus.MBassadorEventBus
import javax.inject.Singleton


@Module
class BaseModule {

    @Provides
    @Singleton
    fun provideEventBus() : IEventBus {
        return MBassadorEventBus()
    }


}
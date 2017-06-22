package net.dankito.deepthought.di

import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BaseModule::class))
interface BaseComponent {

    companion object {
        lateinit var component: BaseComponent
    }

}
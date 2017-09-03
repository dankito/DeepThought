package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.extensions.ReferencePreviewCache
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BaseModule::class))
interface BaseComponent {

    companion object {
        lateinit var component: BaseComponent
    }


    fun inject(referencePreviewCache: ReferencePreviewCache)

}
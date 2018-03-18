package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.data.SeriesPersister
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BaseModule::class))
interface BaseComponent {

    companion object {
        lateinit var component: BaseComponent
    }


    fun inject(itemPersister: ItemPersister)

    fun inject(sourcePersister: SourcePersister)

    fun inject(seriesPersister: SeriesPersister)

}
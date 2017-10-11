package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.data.SeriesPersister
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BaseModule::class))
interface BaseComponent {

    companion object {
        lateinit var component: BaseComponent
    }


    fun inject(entryPersister: EntryPersister)

    fun inject(referencePersister: ReferencePersister)

    fun inject(seriesPersister: SeriesPersister)

}
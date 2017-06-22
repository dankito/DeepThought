package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(CommonModule::class, CommonDataModule::class, BaseModule::class))
interface CommonComponent : BaseComponent {

    companion object {
        lateinit var component: CommonComponent
    }


    fun inject(articleSummaryExtractorConfigManager: ArticleSummaryExtractorConfigManager)

    fun inject(entriesListPresenter: EntriesListPresenter)

    fun inject(tagsListPresenter: TagsListPresenter)

}
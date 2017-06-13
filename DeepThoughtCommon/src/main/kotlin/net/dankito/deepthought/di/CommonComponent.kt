package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.ui.presenter.MainViewPresenter
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(CommonModule::class, CommonDataModule::class))
interface CommonComponent {

    companion object {
        lateinit var component: CommonComponent
    }


    fun inject(articleSummaryExtractorConfigManager: ArticleSummaryExtractorConfigManager)

    fun inject(mainViewPresenter: MainViewPresenter)

}
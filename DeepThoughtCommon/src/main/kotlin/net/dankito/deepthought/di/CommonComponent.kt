package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(CommonModule::class))
interface CommonComponent {

    companion object {
        lateinit var component: CommonComponent
    }


    fun inject(articleSummaryExtractorConfigManager: ArticleSummaryExtractorConfigManager)

}
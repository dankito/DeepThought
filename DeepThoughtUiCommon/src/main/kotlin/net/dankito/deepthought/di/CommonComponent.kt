package net.dankito.deepthought.di

import dagger.Component
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfigManager
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContentDetector
import net.dankito.deepthought.ui.presenter.*
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(CommonModule::class, CommonDataModule::class, BaseModule::class))
interface CommonComponent : BaseComponent {

    companion object {
        lateinit var component: CommonComponent
    }


    fun inject(entriesListPresenter: ItemsListPresenter)

    fun inject(tagsListPresenter: TagsListPresenter)

    fun inject(referencesListPresenter: ReferencesListPresenter)

    fun inject(seriesListPresenter: SeriesListPresenter)

    fun inject(articleSummaryExtractorConfigManager: ArticleSummaryExtractorConfigManager)

    fun inject(articleSummaryPresenter: ArticleSummaryPresenter)

    fun inject(articleSummaryExtractorConfigPresenter: ArticleSummaryExtractorConfigPresenter)

    fun inject(readLaterArticleListPresenter: ReadLaterArticleListPresenter)

    fun inject(editItemPresenter: EditItemPresenter)

    fun inject(tagsOnEntryListPresenter: TagsOnEntryListPresenter)

    fun inject(articleExtractorManager: ArticleExtractorManager)

    fun inject(optionsForClipboardContentDetector: OptionsForClipboardContentDetector)

}
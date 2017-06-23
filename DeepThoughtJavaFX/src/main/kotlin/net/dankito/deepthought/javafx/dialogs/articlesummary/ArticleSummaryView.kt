package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryControlBarView
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import tornadofx.*
import javax.inject.Inject


class ArticleSummaryView : Fragment() {

    @Inject
    protected lateinit var articleExtractors: ArticleExtractors

    @Inject
    protected lateinit var entryPerister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter


    val articleSummaryExtractor: ArticleSummaryExtractorConfig by param()

    private var presenter: ArticleSummaryPresenterJavaFX

    private val articleSummaryItemsView: ArticleSummaryItemsView

    private val articleSummaryControlBarView: ArticleSummaryControlBarView



    override val root = borderpane {

    }


    init {
        AppComponent.component.inject(this)

        presenter = ArticleSummaryPresenterJavaFX(articleSummaryExtractor, articleExtractors, entryPerister, readLaterArticleService, tagService, searchEngine, router)

        articleSummaryItemsView = ArticleSummaryItemsView(presenter)
        root.center = articleSummaryItemsView.root

        articleSummaryControlBarView = ArticleSummaryControlBarView(presenter, articleSummaryItemsView)
        root.bottom = articleSummaryControlBarView.root

        title = articleSummaryExtractor.name
    }

}
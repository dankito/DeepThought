package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.article.ArticleExtractors
import tornadofx.*
import javax.inject.Inject


class ArticleSummaryView : Fragment() {

    @Inject
    protected lateinit var articleExtractors: ArticleExtractors

    @Inject
    protected lateinit var router: IRouter


    override val root = borderpane {

    }

    val articleSummaryExtractor: ArticleSummaryExtractorConfig by param()

    private var controller: ArticleSummaryController


    init {
        AppComponent.component.inject(this)

        controller = ArticleSummaryController(articleSummaryExtractor, articleExtractors, router)

        root.center = ArticleSummaryItemsView(controller).root
    }
}
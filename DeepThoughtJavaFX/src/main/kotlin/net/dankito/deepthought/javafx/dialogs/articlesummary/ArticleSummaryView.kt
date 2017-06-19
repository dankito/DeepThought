package net.dankito.deepthought.javafx.dialogs.articlesummary

import net.dankito.deepthought.javafx.dialogs.articlesummary.controls.ArticleSummaryItemsView
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import tornadofx.*


class ArticleSummaryView : Fragment() {

    private lateinit var articleSummaryItemsView: ArticleSummaryItemsView


    override val root = borderpane {

    }

    val articleSummaryExtractor: ArticleSummaryExtractorConfig by param()

    private lateinit var controller: ArticleSummaryController


    init {
        controller = ArticleSummaryController(articleSummaryExtractor)

        articleSummaryItemsView = ArticleSummaryItemsView(controller)

        root.center = articleSummaryItemsView.root
    }
}
package net.dankito.deepthought.javafx.dialogs.articlesummary

import javafx.collections.FXCollections
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemModel
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.ArticleSummaryItem
import tornadofx.*


class ArticleSummaryController(articleSummaryExtractor: ArticleSummaryExtractorConfig) : Controller() {

    val itemModel = ArticleSummaryItemModel()

    val items = FXCollections.observableArrayList<ArticleSummaryItem>()


    init {
        articleSummaryExtractor.extractor?.extractSummaryAsync {
            it.result?.let { items.setAll(it.articles) }
        }
    }


}
package net.dankito.deepthought.javafx.dialogs.articlesummary

import javafx.collections.FXCollections
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemModel
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import tornadofx.*


/**
 * Yeah, i know this is bad design, a Controller deriving from a Presenter ...
 */
class ArticleSummaryController(articleSummaryExtractor: ArticleSummaryExtractorConfig, articleExtractors: ArticleExtractors, tagService: TagService,
                               searchEngine: ISearchEngine, router: IRouter)
    : ArticleSummaryPresenter(articleExtractors, tagService, searchEngine, router) {

    val itemModel = ArticleSummaryItemModel()

    val items = FXCollections.observableArrayList<ArticleSummaryItem>()


    init {
        extractArticlesSummary(articleSummaryExtractor) {
            it.result?.let { items.setAll(it.articles) }
        }
    }

    fun itemDoubleClicked(selectedItem: ArticleSummaryItem?) {
        selectedItem?.let { item ->
            getAndShowArticle(item) {
                showArticleExtractionError(item, it)
            }
        }
    }

    override fun showArticle(extractionResult: EntryExtractionResult) {
        runLater {
            // ensure Window is started on UI thread
            router.showEditEntryView(extractionResult) // in Java we're not going to ViewEntryView but to EditEntryView
        }
    }

    private fun showArticleExtractionError(item: ArticleSummaryItem, exception: Exception) {

    }


}
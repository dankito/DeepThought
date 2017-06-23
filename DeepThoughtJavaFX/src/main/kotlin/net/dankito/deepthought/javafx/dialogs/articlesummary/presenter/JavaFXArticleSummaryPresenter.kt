package net.dankito.deepthought.javafx.dialogs.articlesummary.presenter

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemModel
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import tornadofx.*


/**
 * Yeah, i know this is bad design, a Controller deriving from a Presenter ...
 */
class JavaFXArticleSummaryPresenter(private val articleSummaryExtractor: ArticleSummaryExtractorConfig, articleExtractors: ArticleExtractors, entryPersister: EntryPersister,
                                    readLaterArticleService: ReadLaterArticleService, tagService: TagService, searchEngine: ISearchEngine, router: IRouter)
    : ArticleSummaryPresenter(articleExtractors, entryPersister, readLaterArticleService, tagService, searchEngine, router) {

    val itemModel = ArticleSummaryItemModel()

    val items = FXCollections.observableArrayList<ArticleSummaryItem>()

    val canLoadMoreItems = SimpleBooleanProperty(false)


    init {
        extractArticlesSummary()
    }


    fun extractArticlesSummary() {
        extractArticlesSummary(articleSummaryExtractor) {
            it.result?.let { articleSummaryReceived(it, false) }
            // TODO: show error elsewise
        }
    }

    fun loadMoreItems() {
        loadMoreItems(articleSummaryExtractor) {
            it.result?.let { articleSummaryReceived(it, true) }
            // TODO: show error elsewise
        }
    }

    private fun articleSummaryReceived(articleSummary: ArticleSummary, hasLoadedMoreItems: Boolean) {
        runLater {
            if(hasLoadedMoreItems) {
                items.addAll(articleSummary.articles)
            }
            else {
                items.setAll(articleSummary.articles)
            }

            canLoadMoreItems.set(articleSummary.canLoadMoreItems)
        }
    }


    fun getAndShowArticle(item: ArticleSummaryItem) {
        getAndShowArticle(item) {
            showArticleExtractionError(item, it)
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
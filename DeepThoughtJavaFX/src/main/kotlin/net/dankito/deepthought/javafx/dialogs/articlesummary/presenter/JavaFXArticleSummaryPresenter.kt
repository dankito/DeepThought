package net.dankito.deepthought.javafx.dialogs.articlesummary.presenter

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemViewModel
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import java.text.DateFormat
import java.util.*


/**
 * Yeah, i know this is bad design, a Controller deriving from a Presenter ...
 */
class JavaFXArticleSummaryPresenter(private val articleSummaryExtractor: ArticleSummaryExtractorConfig, articleExtractors: ArticleExtractors, entryPersister: EntryPersister,
                                    readLaterArticleService: ReadLaterArticleService, tagService: TagService, searchEngine: ISearchEngine, router: IRouter, dialogService: IDialogService)
    : ArticleSummaryPresenter(articleExtractors, entryPersister, readLaterArticleService, tagService, searchEngine, router, dialogService) {


    companion object {
        private val LastUpdateTimeDateFormat = DateFormat.getDateTimeInstance()
    }


    val itemModel = ArticleSummaryItemViewModel()

    val items = FXCollections.observableArrayList<ArticleSummaryItem>()

    val canLoadMoreItems = SimpleBooleanProperty(false)

    val lastUpdateTime = SimpleStringProperty("")


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

            lastUpdateTime.set(LastUpdateTimeDateFormat.format(Date()))
        }
    }


    fun getAndShowArticle(item: ArticleSummaryItem) {
        getAndShowArticle(item) {
            showArticleExtractionError(item, it)
        }
    }

    private fun showArticleExtractionError(item: ArticleSummaryItem, exception: Exception) {

    }


}
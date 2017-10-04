package net.dankito.deepthought.javafx.dialogs.articlesummary.presenter

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemViewModel
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ArticleSummaryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import java.text.DateFormat
import java.util.*


class JavaFXArticleSummaryPresenter(entryPersister: EntryPersister, readLaterArticleService: ReadLaterArticleService,
                                    articleExtractorManager: ArticleExtractorManager, router: IRouter, clipboardService: IClipboardService, dialogService: IDialogService)
    : ArticleSummaryPresenter(entryPersister, readLaterArticleService, articleExtractorManager, router, clipboardService, dialogService) {


    companion object {
        private val LastUpdateTimeDateFormat = DateFormat.getDateTimeInstance()
    }


    private var articleSummaryExtractorConfig: ArticleSummaryExtractorConfig by singleAssign()


    val itemModel = ArticleSummaryItemViewModel()

    val items = FXCollections.observableArrayList<ArticleSummaryItem>()

    val canLoadMoreItems = SimpleBooleanProperty(false)

    val lastUpdateTime = SimpleStringProperty("")


    fun extractArticlesSummary(config: ArticleSummaryExtractorConfig) {
        this.articleSummaryExtractorConfig = config

        extractArticlesSummary(null)
    }

    fun extractArticlesSummary(listView: ListView<ArticleSummaryItem>? = null) {
        extractArticlesSummary(articleSummaryExtractorConfig) {
            it.result?.let { articleSummaryReceived(it, listView) }
        }
    }

    fun loadMoreItems(listView: ListView<ArticleSummaryItem>? = null) {
        lastLoadedSummary?.let { summary ->
            canLoadMoreItems.set(false)

            loadMoreItems(articleSummaryExtractorConfig) {
                it.result?.let { articleSummaryReceived(it, listView) }
            }
        }
    }

    private fun articleSummaryReceived(articleSummary: ArticleSummary, listView: ListView<ArticleSummaryItem>? = null) {
        runLater {
            items.setAll(articleSummary.articles)

            canLoadMoreItems.set(articleSummary.canLoadMoreItems)

            lastUpdateTime.set(LastUpdateTimeDateFormat.format(Date()))

            listView?.let { it.scrollTo(articleSummary.indexOfAddedItems) }
        }
    }


}
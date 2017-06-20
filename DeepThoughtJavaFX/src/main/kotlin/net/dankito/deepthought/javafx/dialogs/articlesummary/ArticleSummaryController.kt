package net.dankito.deepthought.javafx.dialogs.articlesummary

import javafx.collections.FXCollections
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.articlesummary.model.ArticleSummaryItemModel
import net.dankito.deepthought.news.summary.config.ArticleSummaryExtractorConfig
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.EntryExtractionResult
import tornadofx.*
import javax.inject.Inject


class ArticleSummaryController(articleSummaryExtractor: ArticleSummaryExtractorConfig) : Controller() {

    val itemModel = ArticleSummaryItemModel()

    val items = FXCollections.observableArrayList<ArticleSummaryItem>()


    @Inject
    protected lateinit var articleExtractors: ArticleExtractors

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)

        articleSummaryExtractor.extractor?.extractSummaryAsync {
            it.result?.let { items.setAll(it.articles) }
        }
    }

    fun itemDoubleClicked(selectedItem: ArticleSummaryItem?) {
        selectedItem?.let { item ->
            articleExtractors.getExtractorForItem(item)?.let { extractor ->
                extractor.extractArticleAsync(item) { asyncResult ->
                    asyncResult.result?.let { showArticle(it) }
                    asyncResult.error?.let { showArticleExtractionError(item, it) }
                }
            }
        }
    }

    private fun showArticle(result: EntryExtractionResult) {
        runLater {
            router.showEditEntryView(result)
        }
    }

    private fun showArticleExtractionError(item: ArticleSummaryItem, exception: Exception) {

    }


}
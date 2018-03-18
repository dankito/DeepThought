package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import javax.inject.Inject


class EditItemExtractionResultView : EditItemViewBase() {

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager


    val extractionResult: ItemExtractionResult by param()


    init {
        AppComponent.component.inject(this)

        canAlwaysBeSaved = true

        showExtractionResult()
    }


    private fun showExtractionResult() {
        val content = if (extractionResult.couldExtractContent) extractionResult.item.content else ""
        showData(extractionResult.item, extractionResult.tags, extractionResult.source, extractionResult.series, extractionResult.files, content)
    }


    override fun urlLoaded(url: String, html: String) {
        super.urlLoaded(url, html)

        articleExtractorManager.extractArticleUserDidSeeBefore(extractionResult, html, url)

        if(extractionResult.couldExtractContent == false) { // then load full web site html into editor
            extractionResult.couldExtractContent = true
            extractionResult.item.content = html
        }

        showExtractionResult()
    }


    override fun resetSeries() {
        super.resetSeries()

        extractionResult.series = null
    }


}
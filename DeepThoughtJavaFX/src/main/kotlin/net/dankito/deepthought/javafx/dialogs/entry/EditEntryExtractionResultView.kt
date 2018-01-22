package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import javax.inject.Inject


class EditEntryExtractionResultView : EditEntryViewBase() {

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

        if(extractionResult.couldExtractContent) { // now content could get extracted, therefore show it
            showExtractionResult()
        }
    }


    override fun resetSeries() {
        super.resetSeries()

        extractionResult.series = null
    }


}
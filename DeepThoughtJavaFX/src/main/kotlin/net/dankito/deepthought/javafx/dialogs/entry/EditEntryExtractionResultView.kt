package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import javax.inject.Inject


class EditEntryExtractionResultView : EditEntryViewBase() {

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager


    val extractionResult: EntryExtractionResult by param()


    init {
        AppComponent.component.inject(this)

        showExtractionResult()
    }


    private fun showExtractionResult() {
        val content = if (extractionResult.couldExtractContent) extractionResult.entry.content else ""
        showData(extractionResult.entry, extractionResult.tags, extractionResult.reference, extractionResult.series, content)
    }


    override fun urlLoaded(url: String, html: String) {
        super.urlLoaded(url, html)

        articleExtractorManager.extractArticleAndAddDefaultData(extractionResult, html, url)

        if(extractionResult.couldExtractContent) { // now content could get extracted, therefore show it
            showExtractionResult()
        }
    }


}
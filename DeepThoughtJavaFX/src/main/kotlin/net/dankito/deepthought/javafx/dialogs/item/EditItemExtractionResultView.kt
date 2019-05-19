package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import javax.inject.Inject


class EditItemExtractionResultView : EditItemViewBase() {

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager


    private lateinit var extractionResult: ItemExtractionResult


    init {
        AppComponent.component.inject(this)

        canAlwaysBeSaved = true

        (windowData as? ItemExtractionResult)?.let { extractionResult ->
            this.extractionResult = extractionResult

            showExtractionResult()
        }
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


    private fun showExtractionResult() {
        val content = if (extractionResult.couldExtractContent) extractionResult.item.content else ""
        showData(extractionResult.item, extractionResult.tags, extractionResult.source, extractionResult.series, extractionResult.files, content)
    }

    override fun resetSeries() {
        super.resetSeries()

        extractionResult.series = null
    }


    override val windowDataClass = ItemExtractionResult::class.java

    // TODO: update extractionResult before, e.g. content with htmlEditor.getCurrentHtmlBlocking()
    override fun getCurrentWindowData() = extractionResult

    override val displayText: CharSequence
        get() = extractionResult.source?.getPreviewWithSeriesAndPublishingDate(extractionResult.series) ?: super.displayText


//    private fun openActivityClicked(clickedActivity: BaseActivity) {
//        clickedActivity.intent?.let { intent ->
//            //            intent.setFlags(intent.flags.rem(Intent.FLAG_ACTIVITY_NEW_TASK))
//
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
////            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//
//            if (clickedActivity.isFinishing || clickedActivity.isDestroyed) {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
////            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
////            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//            startActivity(intent)
//        }
//
//    }

}
package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.windowdata.EditItemExtractionResultWindowData
import javax.inject.Inject


class EditItemExtractionResultView : EditItemViewBase() {

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager


    private lateinit var extractionResultWindowData: EditItemExtractionResultWindowData

    private val extractionResult: ItemExtractionResult
        get() = extractionResultWindowData.itemExtractionResult


    init {
        AppComponent.component.inject(this)

        canAlwaysBeSaved = true

        (windowData as? EditItemExtractionResultWindowData)?.let { extractionResultWindowData ->
            this.extractionResultWindowData = extractionResultWindowData

            showExtractionResult()
            restoreWindowData(extractionResultWindowData)
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


    override val windowDataClass = EditItemExtractionResultWindowData::class.java

    override fun getCurrentWindowData(): Any? {
        updateWindowData(extractionResultWindowData)

        return extractionResultWindowData
    }

    override val displayText: CharSequence
        get() = (windowData as? EditItemExtractionResultWindowData)?.itemExtractionResult?.source?.getPreviewWithSeriesAndPublishingDate(extractionResult.series)
                ?: super.displayText


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
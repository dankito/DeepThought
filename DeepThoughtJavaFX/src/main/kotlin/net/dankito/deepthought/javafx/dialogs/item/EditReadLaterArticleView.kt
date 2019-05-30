package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.ui.windowdata.EditReadLaterArticleWindowData


class EditReadLaterArticleView : EditItemViewBase() {

    private lateinit var editReadLaterArticleWindowData: EditReadLaterArticleWindowData


    init {
        AppComponent.component.inject(this)

        canAlwaysBeSaved = true

        (windowData as? EditReadLaterArticleWindowData)?.let { editReadLaterArticleWindowData ->
            this.editReadLaterArticleWindowData = editReadLaterArticleWindowData
            val article = editReadLaterArticleWindowData.readLaterArticle

            readLaterArticleService.deserializeItemExtractionResult(article)

            val extractionResult = article.itemExtractionResult

            showData(extractionResult.item, extractionResult.tags, extractionResult.source, extractionResult.series, extractionResult.files)
            restoreWindowData(editReadLaterArticleWindowData)
        }
    }


    override fun resetSeries() {
        super.resetSeries()

        editReadLaterArticleWindowData.readLaterArticle.itemExtractionResult.series = null
    }

    override fun itemSaved() {
        super.itemSaved()

        readLaterArticleService.delete(editReadLaterArticleWindowData.readLaterArticle)
    }


    override val windowDataClass = EditReadLaterArticleWindowData::class.java

    override fun getCurrentWindowData(): Any? {
        updateWindowData(editReadLaterArticleWindowData)

        return editReadLaterArticleWindowData
    }


}
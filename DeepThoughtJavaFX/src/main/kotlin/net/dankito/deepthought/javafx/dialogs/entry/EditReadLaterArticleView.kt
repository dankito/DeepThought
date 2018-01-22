package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.ReadLaterArticle


class EditReadLaterArticleView : EditEntryViewBase() {

    val article: ReadLaterArticle by param()


    init {
        AppComponent.component.inject(this)

        canAlwaysBeSaved = true

        val extractionResult = article.itemExtractionResult

        showData(extractionResult.item, extractionResult.tags, extractionResult.source, extractionResult.series, extractionResult.files)
    }


    override fun resetSeries() {
        super.resetSeries()

        article.itemExtractionResult.series = null
    }

    override fun entrySaved() {
        super.entrySaved()

        readLaterArticleService.delete(article)
    }


}
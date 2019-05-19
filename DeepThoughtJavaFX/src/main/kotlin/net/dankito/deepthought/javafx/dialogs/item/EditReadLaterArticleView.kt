package net.dankito.deepthought.javafx.dialogs.item

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.ReadLaterArticle


class EditReadLaterArticleView : EditItemViewBase() {

    private lateinit var article: ReadLaterArticle


    init {
        AppComponent.component.inject(this)

        canAlwaysBeSaved = true

        (windowData as? ReadLaterArticle)?.let { article ->
            this.article = article

            readLaterArticleService.deserializeItemExtractionResult(article)

            val extractionResult = article.itemExtractionResult

            showData(extractionResult.item, extractionResult.tags, extractionResult.source, extractionResult.series, extractionResult.files)
        }
    }


    override fun resetSeries() {
        super.resetSeries()

        article.itemExtractionResult.series = null
    }

    override fun itemSaved() {
        super.itemSaved()

        readLaterArticleService.delete(article)
    }


    override val windowDataClass = ReadLaterArticle::class.java

    override fun getCurrentWindowData() = article


}
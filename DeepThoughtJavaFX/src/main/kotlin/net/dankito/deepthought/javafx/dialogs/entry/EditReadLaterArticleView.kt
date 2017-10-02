package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.*


class EditReadLaterArticleView : EditEntryViewBase() {

    val article: ReadLaterArticle by param()


    init {
        AppComponent.component.inject(this)

        showData(article.entryExtractionResult.entry, article.entryExtractionResult.tags, article.entryExtractionResult.reference, article.entryExtractionResult.series)
    }


    override fun getEntryForSaving(): Entry {
        return article.entryExtractionResult.entry
    }

    override fun getReferenceForSaving(): Reference? {
        return article.entryExtractionResult.reference
    }

    override fun getCurrentSeries(): Series? {
        return article.entryExtractionResult.series
    }

    override fun getTagsForSaving(): List<Tag> {
        return article.entryExtractionResult.tags
    }


    override fun entrySaved() {
        super.entrySaved()

        readLaterArticleService.delete(article)
    }


}
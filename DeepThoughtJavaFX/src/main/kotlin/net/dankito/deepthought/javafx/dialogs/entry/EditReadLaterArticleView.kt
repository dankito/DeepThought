package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.ReadLaterArticle


class EditReadLaterArticleView : EditEntryViewBase() {

    val article: ReadLaterArticle by param()


    init {
        AppComponent.component.inject(this)

        showData(article.entryExtractionResult.entry, article.entryExtractionResult.tags, article.entryExtractionResult.reference, article.entryExtractionResult.series)
    }


    override fun entrySaved() {
        super.entrySaved()

        readLaterArticleService.delete(article)
    }


}
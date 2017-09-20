package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.*
import org.jsoup.Jsoup


class EditReadLaterArticleView : EditEntryViewBase() {

    val article: ReadLaterArticle by param()


    init {
        AppComponent.component.inject(this)

        abstractPlainText.value = Jsoup.parseBodyFragment(article.entryExtractionResult.entry.abstractString).text()
        contentHtml.value = article.entryExtractionResult.entry.content
        showReferencePreview(article.entryExtractionResult.reference)
        showTagsPreview(article.entryExtractionResult.tags)
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
package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.ReadLaterArticleService
import org.jsoup.Jsoup
import javax.inject.Inject


class EditReadLaterArticleView : EditEntryViewBase() {

    val article: ReadLaterArticle by param()


    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService


    init {
        AppComponent.component.inject(this)

        hasAbstract.value = article.entryExtractionResult.entry.hasAbstract()
        abstractPlainText.value = Jsoup.parseBodyFragment(article.entryExtractionResult.entry.abstractString).text()
        contentHtml.value = article.entryExtractionResult.entry.content
    }


    override fun getEntryForSaving(): Entry {
        return article.entryExtractionResult.entry
    }

    override fun getReferenceForSaving(): Reference? {
        return article.entryExtractionResult.reference
    }

    override fun getTagsForSaving(): List<Tag> {
        return article.entryExtractionResult.tags
    }


    override fun entrySaved() {
        super.entrySaved()

        readLaterArticleService.delete(article)
    }


}
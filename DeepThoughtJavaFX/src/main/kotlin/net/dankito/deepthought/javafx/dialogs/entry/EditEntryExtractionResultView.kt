package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.Jsoup


class EditEntryExtractionResultView : EditEntryViewBase() {

    val extractionResult: EntryExtractionResult by param()


    init {
        hasAbstract.value = extractionResult.entry.hasAbstract()
        abstractPlainText.value = Jsoup.parseBodyFragment(extractionResult.entry.abstractString).text()
        contentHtml.value = extractionResult.entry.content
    }


    override fun getEntryForSaving(): Entry {
        return extractionResult.entry
    }

    override fun getReferenceForSaving(): Reference? {
        return extractionResult.reference
    }

    override fun getTagsForSaving(): List<Tag> {
        return extractionResult.tags
    }


}
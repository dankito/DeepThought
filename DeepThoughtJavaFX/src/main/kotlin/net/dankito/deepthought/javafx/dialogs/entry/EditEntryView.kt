package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import org.jsoup.Jsoup


class EditEntryView : EditEntryViewBase() {

    val entry: Entry by param()


    init {
        abstractPlainText.value = Jsoup.parseBodyFragment(entry.abstractString).text()
        contentHtml.value = entry.content
        showReferencePreview(entry.reference)
        showTagsPreview(entry.tags)

        hasUnsavedChanges.value = false
    }


    override fun getEntryForSaving(): Entry {
        return entry
    }

    override fun getReferenceForSaving(): Reference? {
        return entry.reference
    }

    override fun getCurrentSeries(): Series? {
        return entry.reference?.series
    }

    override fun getTagsForSaving(): List<Tag> {
        return listOf()
    }

}
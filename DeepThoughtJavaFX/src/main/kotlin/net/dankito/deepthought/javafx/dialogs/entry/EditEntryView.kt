package net.dankito.deepthought.javafx.dialogs.entry

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import org.jsoup.Jsoup


class EditEntryView : EditEntryViewBase() {

    val entry: Entry by param()


    init {
        hasAbstract.value = entry.hasAbstract()
        abstractPlainText.value = Jsoup.parseBodyFragment(entry.abstractString).text()
        contentHtml.value = entry.content

        hasUnsavedChanges.value = false
    }


    override fun getEntryForSaving(): Entry {
        return entry
    }

    override fun getReferenceForSaving(): Reference? {
        return entry.reference
    }

    override fun getTagsForSaving(): List<Tag> {
        return listOf()
    }

}
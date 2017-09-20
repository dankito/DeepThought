package net.dankito.deepthought.model.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag


data class EntryExtractionResult(var entry: Entry, var reference: Reference? = null, var series: Series? = null, var tags: MutableList<Tag> = ArrayList(),
                                 var couldExtractContent: Boolean = false,
                                 var webSiteHtml: String? = null,
                                 var error: Exception? = null) {


    private constructor() : this(Entry("")) // for Jackson


    fun setExtractedContent(entry: Entry, reference: Reference?) {
        val previousEntry = this.entry
        val previousReference = this.reference

        this.entry = entry
        reference?.let { this.reference = it }

        copyInfoFromPreviousEntryAndReference(entry, previousEntry, reference, previousReference)

        this.couldExtractContent = true
    }

    private fun copyInfoFromPreviousEntryAndReference(entry: Entry, previousEntry: Entry, reference: Reference?, previousReference: Reference?) {
        if (entry.abstractString.isNullOrBlank() && previousEntry != null) {
            entry.abstractString = previousEntry.abstractString
        }

        if (reference != null && reference.previewImageUrl == null && previousReference?.previewImageUrl != null) {
            reference.previewImageUrl = previousReference?.previewImageUrl
        }
    }

}
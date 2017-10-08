package net.dankito.deepthought.model.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag


data class EntryExtractionResult(var entry: Entry, var reference: Reference? = null, var series: Series? = null, var tags: MutableList<Tag> = ArrayList(),
                                 var couldExtractContent: Boolean = false,
                                 var error: Exception? = null) {


    private constructor() : this(Entry("")) // for Jackson


    fun setExtractedContent(entry: Entry, reference: Reference?) {
        val previousEntry = this.entry
        val previousReference = this.reference

        this.entry = entry
        reference?.let { this.reference = it }

        this.couldExtractContent = entry.content.isNullOrBlank() == false // set couldExtractContent here by extracted content before previous content may gets copied over in copyInfoFromPreviousEntryAndReference()

        copyInfoFromPreviousEntryAndReference(entry, previousEntry, reference, previousReference)
    }

    private fun copyInfoFromPreviousEntryAndReference(entry: Entry, previousEntry: Entry, reference: Reference?, previousReference: Reference?) {
        if(entry.abstractString.isNullOrBlank() && previousEntry != null) {
            entry.abstractString = previousEntry.abstractString
        }
        if(entry.content.isNullOrBlank() && previousEntry != null) {
            entry.content = previousEntry.content
        }

        if(reference != null && previousReference != null) {
            if(reference.title.isNullOrBlank() && previousReference.title.isNullOrBlank() == false) {
                reference.title = previousReference.title
            }
            if(reference.previewImageUrl == null && previousReference.previewImageUrl != null) {
                reference.previewImageUrl = previousReference.previewImageUrl
            }
            if(reference.publishingDate == null && previousReference.publishingDate != null) {
                reference.publishingDate = previousReference.publishingDate
            }
            if(reference.publishingDateString.isNullOrBlank() && previousReference.publishingDateString.isNullOrBlank() == false) {
                reference.setPublishingDate(reference.publishingDate, previousReference.publishingDateString)
            }
        }
    }

}
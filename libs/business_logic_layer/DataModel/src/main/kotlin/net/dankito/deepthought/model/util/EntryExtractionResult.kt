package net.dankito.deepthought.model.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag


data class EntryExtractionResult(var entry: Entry, var reference: Reference? = null, var tags: MutableList<Tag> = ArrayList(),
                                 var couldExtractContent: Boolean = false,
                                 var webSiteHtml: String? = null,
                                 var error: Exception? = null) {


    private constructor() : this(Entry("")) // for Jackson


    fun setExtractedContent(entry: Entry, reference: Reference?) {
        this.entry = entry
        reference?.let { this.reference = it }

        this.couldExtractContent = true
    }

}
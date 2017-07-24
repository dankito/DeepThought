package net.dankito.deepthought.model.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag


data class EntryExtractionResult(val entry: Entry, var reference: Reference? = null, val tags: MutableList<Tag> = ArrayList()) {


    private constructor() : this(Entry("")) // for Jackson
}
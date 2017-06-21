package net.dankito.newsreader.model

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag


data class EntryExtractionResult(val entry: Entry, val reference: Reference? = null, val tags: MutableList<Tag> = ArrayList()) {


    private constructor() : this(Entry("")) // for Jackson
}
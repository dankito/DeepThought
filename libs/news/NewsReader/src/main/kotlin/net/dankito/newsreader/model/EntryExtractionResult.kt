package net.dankito.newsreader.model

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference


data class EntryExtractionResult(val entry: Entry, val reference: Reference? = null)
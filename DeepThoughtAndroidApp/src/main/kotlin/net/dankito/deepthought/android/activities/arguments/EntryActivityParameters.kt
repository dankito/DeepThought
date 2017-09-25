package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.EntryExtractionResult


data class EntryActivityParameters(val entry: Entry? = null,
                                   val readLaterArticle: ReadLaterArticle? = null,
                                   val entryExtractionResult: EntryExtractionResult? = null,
                                   val createEntry: Boolean = false)
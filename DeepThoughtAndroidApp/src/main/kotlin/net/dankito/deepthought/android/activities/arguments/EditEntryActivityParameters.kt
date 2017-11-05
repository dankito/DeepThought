package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.util.ItemExtractionResult


data class EditEntryActivityParameters(val item: Item? = null,
                                       val readLaterArticle: ReadLaterArticle? = null,
                                       val itemExtractionResult: ItemExtractionResult? = null,
                                       val createEntry: Boolean = false)
package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Entry


data class EditEntryActivityResult(val didSaveEntry: Boolean = false, val didSaveReadLaterArticle: Boolean = false, val didSaveEntryExtractionResult: Boolean = false,
                                   val savedEntry: Entry? = null)
package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Item


data class EditItemActivityResult(val didSaveItem: Boolean = false, val didSaveForLaterReading: Boolean = false, val savedItem: Item? = null)
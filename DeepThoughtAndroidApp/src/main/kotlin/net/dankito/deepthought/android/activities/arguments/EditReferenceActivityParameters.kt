package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Series


data class EditReferenceActivityParameters(val source: Source?, val forItem: Item? = null, val series: Series? = null)
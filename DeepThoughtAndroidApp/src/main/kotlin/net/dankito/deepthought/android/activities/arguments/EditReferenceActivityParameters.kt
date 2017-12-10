package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source


data class EditReferenceActivityParameters(val source: Source?, val series: Series? = null, val editedSourceTitle: String? = null)
package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series


data class EditSeriesActivityParameters(val series: Series?, val forReference: Reference? = null)
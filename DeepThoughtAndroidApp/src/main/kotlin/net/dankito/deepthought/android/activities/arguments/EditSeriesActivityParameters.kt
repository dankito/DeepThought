package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Series


data class EditSeriesActivityParameters(val series: Series?, val forSource: Source? = null)
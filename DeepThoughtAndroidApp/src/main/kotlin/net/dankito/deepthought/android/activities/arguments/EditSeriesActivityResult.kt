package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Series


data class EditSeriesActivityResult(val didSaveSeries: Boolean = false, val savedSeries: Series? = null)
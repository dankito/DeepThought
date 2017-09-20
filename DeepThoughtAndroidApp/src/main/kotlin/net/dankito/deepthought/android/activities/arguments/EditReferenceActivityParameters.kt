package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series


data class EditReferenceActivityParameters(val reference: Reference?, val ofEntry: Entry? = null, val series: Series? = null)
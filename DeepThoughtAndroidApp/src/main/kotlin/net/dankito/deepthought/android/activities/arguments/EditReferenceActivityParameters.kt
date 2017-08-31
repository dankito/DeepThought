package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference


data class EditReferenceActivityParameters(val reference: Reference?, val ofEntry: Entry? = null)
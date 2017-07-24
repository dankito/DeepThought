package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Reference


data class EditReferenceActivityResult(val didSaveReference: Boolean = false, val savedReference: Reference? = null)
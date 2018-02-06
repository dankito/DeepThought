package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Source


data class EditSourceActivityResult(val didSaveSource: Boolean = false, val savedSource: Source? = null, val didDeleteSource: Boolean = false)
package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.DeepThoughtFileLink
import net.dankito.deepthought.model.Source
import java.io.File


/**
 * Either persistedPdfFile or addNewPdfFile may be != null.
 * It has to be that complicated to be able to remove FileManager (which needs a loaded DataManager) can be removed from MainActivity.
 */
data class ViewPdfActivityParameters(val persistedPdfFile: DeepThoughtFileLink?, val addNewPdfFile: File?, val sourceForFile: Source? = null)
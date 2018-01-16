package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Source


data class ViewPdfActivityParameters(val pdfFile: FileLink, val sourceForFile: Source? = null)
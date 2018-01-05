package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.Source
import java.io.File


data class ViewPdfActivityParameters(val pdfFile: File, val sourceForFile: Source? = null)
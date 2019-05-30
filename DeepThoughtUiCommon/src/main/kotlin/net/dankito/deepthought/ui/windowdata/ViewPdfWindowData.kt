package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Source
import java.io.File


class ViewPdfWindowData(val addNewPdfFile: File? = null, val persistedPdfFile: FileLink? = null, var sourceForFile: Source? = null)
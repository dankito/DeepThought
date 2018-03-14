package net.dankito.deepthought.android.views

import android.view.View
import android.view.ViewGroup
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult


interface IEditItemView {

    val currentSource: Source?

    val viewToolbar: ViewGroup

    val itemFieldsPreview: View


    fun getItemExtractionResult(): ItemExtractionResult?

    fun setFloatingActionButtonVisibilityOnUIThread()

    fun extractedContentOnUiThread(extractionResult: ItemExtractionResult)


}
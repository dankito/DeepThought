package net.dankito.deepthought.android.views

import android.support.design.widget.AppBarLayout
import android.view.View
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult


interface IEditItemView {

    val currentSource: Source?

    val appBar: AppBarLayout

    val itemFieldsPreview: View


    fun getItemExtractionResult(): ItemExtractionResult?

    fun setFloatingActionButtonVisibilityOnUIThread()

    fun extractedContentOnUiThread(extractionResult: ItemExtractionResult)


}
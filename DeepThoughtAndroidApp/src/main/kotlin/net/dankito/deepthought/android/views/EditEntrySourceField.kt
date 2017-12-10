package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import net.dankito.deepthought.android.R
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate


class EditEntrySourceField : EditEntityEntityReferenceField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private var source: Source? = null

    private var series: Series? = null


    init {
        setFieldNameOnUiThread(R.string.activity_edit_item_source_label)
    }


    fun setSourceToEdit(source: Source?, series: Series?) {
        this.source = source
        this.series = series

        setFieldValueOnUiThread(source?.title ?: "", source.getPreviewWithSeriesAndPublishingDate(series))
    }

    override fun editTextLostFocus() {
        super.editTextLostFocus()

        val editedTitle = getCurrentFieldValue()
        val editedSourcePreview = Source(editedTitle, "", publishingDate = source?.publishingDate).getPreviewWithSeriesAndPublishingDate(series)

        setValues(editedTitle, editedSourcePreview)
        setDisplayedValue(false)
    }

}
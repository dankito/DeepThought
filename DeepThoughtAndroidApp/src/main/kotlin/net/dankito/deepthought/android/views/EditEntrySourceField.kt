package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.activities.EditReferenceActivity
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.ui.IRouter


class EditEntrySourceField : EditEntityEntityReferenceField {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private var source: Source? = null

    private var series: Series? = null

    private var router: IRouter? = null

    private var activity: BaseActivity? = null

    private var sourceChangedListener: ((Source?) -> Unit)? = null


    init {
        setFieldNameOnUiThread(R.string.activity_edit_item_source_label)
    }


    fun setSourceToEdit(source: Source?, series: Series?, router: IRouter, activity: BaseActivity, sourceChangedListener: (Source?) -> Unit) {
        this.source = source
        this.series = series
        this.router = router
        this.activity = activity
        this.sourceChangedListener = sourceChangedListener

        setFieldValueOnUiThread(source?.title ?: "", source.getPreviewWithSeriesAndPublishingDate(series))
    }

    override fun editTextLostFocus() {
        super.editTextLostFocus()

        val editedTitle = getCurrentFieldValue()
        val editedSourcePreview = Source(editedTitle, "", publishingDate = source?.publishingDate).getPreviewWithSeriesAndPublishingDate(series)

        setValues(editedTitle, editedSourcePreview)
        setDisplayedValue(false)
    }


    override fun editDetails() {
        activity?.setWaitingForResult(EditReferenceActivity.ResultId)

        router?.showEditEntryReferenceView(source, series, valueToEdit)
    }

    override fun createNewEntity() {
        sourceChangedListener?.invoke(Source(""))
    }

    override fun removeEntity() {
        txtEntityFieldName.requestFocus() // to remove focus from EditText; therefore focusable is set to true on txtEntityFieldName
        stopEditing()

        sourceChangedListener?.invoke(null)
    }
}
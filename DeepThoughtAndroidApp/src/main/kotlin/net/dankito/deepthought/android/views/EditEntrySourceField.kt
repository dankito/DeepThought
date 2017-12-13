package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.activities.EditReferenceActivity
import net.dankito.deepthought.android.adapter.ReferenceOnEntryRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EditEntrySourceField : EditEntityEntityReferenceField, IReferencesListView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    private var source: Source? = null

    private var series: Series? = null

    private var activity: BaseActivity? = null

    private var sourceChangedListener: ((Source?) -> Unit)? = null


    private val presenter: ReferencesListPresenter

    private val existingSourcesSearchResultsAdapter: ReferenceOnEntryRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        setFieldNameOnUiThread(R.string.activity_edit_item_source_label)

        presenter = ReferencesListPresenter(this, router, searchEngine, clipboardService, deleteEntityService)

        existingSourcesSearchResultsAdapter = ReferenceOnEntryRecyclerAdapter(presenter)
        existingSourcesSearchResultsAdapter.itemClickListener = { item -> existingSourceSelected(item) }

        rcySearchResult.adapter = existingSourcesSearchResultsAdapter
        rcySearchResult.maxHeightInPixel = (context.resources.getDimension(R.dimen.list_item_reference_height) * 3.25).toInt() // show at max three list items and a little bit from the next item so that user knows there's more
    }


    fun setSourceToEdit(source: Source?, series: Series?, activity: BaseActivity, sourceChangedListener: (Source?) -> Unit) {
        this.source = source
        this.series = series
        this.activity = activity
        this.sourceChangedListener = sourceChangedListener

        existingSourcesSearchResultsAdapter.selectedSource = source

        setFieldValueOnUiThread(source?.title ?: "", source.getPreviewWithSeriesAndPublishingDate(series))
    }

    override fun editTextLostFocus() {
        super.editTextLostFocus()

        val editedTitle = getCurrentFieldValue()
        val editedSourcePreview = Source(editedTitle, "", publishingDate = source?.publishingDate).getPreviewWithSeriesAndPublishingDate(series)

        setValues(editedTitle, editedSourcePreview)
        setDisplayedValue(false)
    }

    override fun hasFocusChanged(hasFocus: Boolean) {
        super.hasFocusChanged(hasFocus)

        if(hasFocus) {
            presenter.searchReferences(presenter.getLastSearchTerm())
        }
    }

    override fun updateValueToEdit() {
        super.updateValueToEdit()

        if(edtxtEntityFieldValue.hasFocus()) {
            presenter.searchReferences(getCurrentFieldValue())
        }
    }


    override fun editDetails() {
        activity?.setWaitingForResult(EditReferenceActivity.ResultId)

        router?.showEditEntryReferenceView(source, series, valueToEdit)
    }

    override fun createNewEntity() {
        sourceChangedListener?.invoke(Source(""))
    }

    override fun removeEntity() {
        stopEditing()

        sourceChangedListener?.invoke(null)
    }

    private fun existingSourceSelected(source: Source) {
        existingSourcesSearchResultsAdapter.selectedSource = source

        sourceChangedListener?.invoke(source)

        rcySearchResult.visibility = View.GONE
    }


    /*      IReferenceListView implementation       */

    override fun showEntities(entities: List<Source>) {
        activity?.runOnUiThread {
            existingSourcesSearchResultsAdapter.items = entities

            rcySearchResult.visibility = if(entities.isEmpty() || edtxtEntityFieldValue.hasFocus() == false) View.GONE else View.VISIBLE
        }
    }

}
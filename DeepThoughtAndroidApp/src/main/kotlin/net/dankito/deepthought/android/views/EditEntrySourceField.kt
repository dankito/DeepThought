package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.activities.EditReferenceActivity
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
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


    fun setOriginalSourceToEdit(source: Source?, series: Series? = source?.series, activity: BaseActivity, sourceChangedListener: (Source?) -> Unit) {
        this.activity = activity
        this.sourceChangedListener = sourceChangedListener

        sourceChanged(source, series)
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

    override fun enteredTextChanged(enteredText: String) {
        super.enteredTextChanged(enteredText)

        if(source == null && enteredText.isNotBlank()) { // user entered a title, but source is null -> create a new Source
            sourceChanged(Source(enteredText))
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
        sourceChanged(Source(""))
    }

    override fun removeEntity() {
        sourceChanged(null)

        stopEditing()
    }

    private fun existingSourceSelected(source: Source) {
        sourceChanged(source)

        hideSearchResultsView()
    }

    fun editingSourceDone(result: EditReferenceActivityResult) {
        if(result.didSaveReference) {
            setFieldValueForCurrentSourceOnUiThread()
        }
        else if(result.didDeleteReference) {
            removeEntity()
        }
    }

    fun sourceChanged(source: Source?, series: Series? = source?.series) {
        this.source = source
        this.series = series

        existingSourcesSearchResultsAdapter.selectedSource = source

        setFieldValueForCurrentSourceOnUiThread()

        sourceChangedListener?.invoke(source)
    }

    private fun setFieldValueForCurrentSourceOnUiThread() {
        setFieldValueOnUiThread(source?.title ?: "", source.getPreviewWithSeriesAndPublishingDate(series))
    }


    /*      IReferenceListView implementation       */

    override fun showEntities(entities: List<Source>) {
        activity?.runOnUiThread {
            existingSourcesSearchResultsAdapter.items = entities

            if(entities.isEmpty() || edtxtEntityFieldValue.hasFocus() == false) {
                hideSearchResultsView()
            }
            else {
                showSearchResultsView()
            }
        }
    }

}
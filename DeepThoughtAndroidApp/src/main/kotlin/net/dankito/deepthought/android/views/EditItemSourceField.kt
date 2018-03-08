package net.dankito.deepthought.android.views

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ViewGroup
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.activities.EditSourceActivity
import net.dankito.deepthought.android.activities.arguments.EditSourceActivityResult
import net.dankito.deepthought.android.adapter.SourceOnItemRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.SourcesListPresenter
import net.dankito.deepthought.ui.view.ISourcesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.SourceService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.ui.IClipboardService
import javax.inject.Inject


class EditItemSourceField : EditEntityEntityReferenceField, ISourcesListView {

    companion object {
        private const val SOURCE_ID_EXTRA_NAME = "SOURCE_ID"
        private const val SOURCE_EXTRA_NAME = "SOURCE"
        private const val SERIES_ID_EXTRA_NAME = "SERIES_ID"
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var sourceService: SourceService

    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    var source: Source? = null
        private set

    var series: Series? = null
        private set

    var originalSource: Source? = null
        private set

    private var activity: BaseActivity? = null

    private var sourceChangedListener: ((Source?) -> Unit)? = null


    private val presenter: SourcesListPresenter

    private val existingSourcesSearchResultsAdapter: SourceOnItemRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        setFieldNameOnUiThread(R.string.edit_entity_source_field_source_label)

        presenter = SourcesListPresenter(this, searchEngine, router, clipboardService, deleteEntityService)

        existingSourcesSearchResultsAdapter = SourceOnItemRecyclerAdapter(presenter)
        existingSourcesSearchResultsAdapter.itemClickListener = { item -> existingSourceSelected(item) }

        rcySearchResult.adapter = existingSourcesSearchResultsAdapter
        rcySearchResult.maxHeightInPixel = (context.resources.getDimension(R.dimen.list_item_source_height) * 3.25).toInt() // show at max three list items and a little bit from the next item so that user knows there's more
    }

    override fun doCustomUiInitialization(rootView: ViewGroup) {
        super.doCustomUiInitialization(rootView)

        setupSecondaryInformation(R.string.edit_entity_source_field_indication_label, R.string.edit_entity_source_field_indication_hint)

        addSecondaryInformationMenuItemTitleResId = R.string.edit_entity_source_field_add_indication_menu_item_title
    }


    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()

        (parcelable as? Bundle)?.let { bundle ->
            val constSource = this.source

            if(constSource != originalSource) {
                if(constSource?.id != null) {
                    bundle.putString(SOURCE_ID_EXTRA_NAME, constSource.id)
                }
                else if(constSource != null) { // a unpersisted source
                    bundle.putString(SOURCE_EXTRA_NAME, serializer.serializeObject(constSource))
                }
            }

            series?.id?.let { seriesId ->
                bundle.putString(SERIES_ID_EXTRA_NAME, seriesId)
            }
        }

        return parcelable
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)

        this.source = null // if source has been null nothing got saved to outState bundle

        (state as? Bundle)?.let { savedInstanceState ->
            savedInstanceState.getString(SOURCE_ID_EXTRA_NAME)?.let { sourceId ->
                this.source = sourceService.retrieve(sourceId)
            }

            savedInstanceState.getString(SOURCE_EXTRA_NAME)?.let { serializedSource ->
                this.source = serializer.deserializeObject(serializedSource, Source::class.java)
            }

            savedInstanceState.getString(SERIES_ID_EXTRA_NAME)?.let { seriesId ->
                this.series = seriesService.retrieve(seriesId)
            }
        }
    }


    fun setOriginalSourceToEdit(source: Source?, series: Series? = source?.series, indication: String, activity: BaseActivity, sourceChangedListener: (Source?) -> Unit) {
        this.originalSource = source
        this.activity = activity
        this.sourceChangedListener = sourceChangedListener

        if(indication.isNotEmpty()) {
            showSecondaryInformationValueOnUiThread(indication)
        }

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
            presenter.searchSources(presenter.getLastSearchTerm())
        }
    }

    override fun enteredTextChanged(enteredText: String) {
        if(source == null && enteredText.isNotBlank()) { // user entered a title, but source is null -> create a new Source
            sourceChanged(Source(enteredText))
        }

        super.enteredTextChanged(enteredText)
    }

    override fun updateValueToEdit() {
        super.updateValueToEdit()

        if(edtxtEntityFieldValue.hasFocus()) {
            presenter.searchSources(getCurrentFieldValue())
        }
    }


    override fun editDetails() {
        activity?.setWaitingForResult(EditSourceActivity.ResultId)

        router.showEditItemSourceView(source, series, valueToEdit)
    }

    override fun createNewEntity() {
        sourceChanged(Source(""))

        startEditing()
    }

    override fun removeEntity() {
        sourceChanged(null)

        stopEditing()
    }

    private fun existingSourceSelected(source: Source) {
        sourceChanged(source)

        hideSearchResultsView()
    }

    fun editingSourceDone(result: EditSourceActivityResult) {
        if(result.didSaveSource) {
            sourceChanged(result.savedSource)
        }
        else if(result.didDeleteSource) {
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


    override fun viewBecomesVisible() {
        super.viewBecomesVisible()

        presenter.viewBecomesVisible()
    }

    override fun viewGetsHidden() {
        presenter.viewGetsHidden()

        super.viewGetsHidden()
    }


    /*      ISourceListView implementation       */

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
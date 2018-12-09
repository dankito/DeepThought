package net.dankito.deepthought.android.views

import android.content.Context
import android.util.AttributeSet
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.BaseActivity
import net.dankito.deepthought.android.activities.EditSeriesActivity
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityResult
import net.dankito.deepthought.android.adapter.SeriesOnSourceRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.SeriesListPresenter
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import javax.inject.Inject


class EditSourceSeriesField : EditEntityEntityReferenceField, ISeriesListView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    private var series: Series? = null

    private var activity: BaseActivity? = null

    private var seriesChangedListener: ((Series?) -> Unit)? = null


    private val presenter: SeriesListPresenter

    private val existingSeriesSearchResultsAdapter: SeriesOnSourceRecyclerAdapter


    init {
        AppComponent.component.inject(this)

        setFieldNameOnUiThread(R.string.activity_edit_source_series_label)

        presenter = SeriesListPresenter(this, searchEngine, router, deleteEntityService)

        existingSeriesSearchResultsAdapter = SeriesOnSourceRecyclerAdapter(presenter)
        existingSeriesSearchResultsAdapter.itemClickListener = { item -> existingSeriesSelected(item) }

        rcySearchResult.adapter = existingSeriesSearchResultsAdapter
        rcySearchResult.maxHeightInPixel = (context.resources.getDimension(R.dimen.list_item_series_height) * 5.25).toInt() // show at max five list items and a little bit from the next item so that user knows there's more

        showEditDetailsMenuItem = false
    }


    override fun viewBecomesVisible() {
        super.viewBecomesVisible()

        presenter.viewBecomesVisible()
    }

    override fun viewGetsHidden() {
        presenter.viewGetsHidden()

        super.viewGetsHidden()
    }


    fun setOriginalSeriesToEdit(series: Series?, activity: BaseActivity, seriesChangedListener: (Series?) -> Unit) {
        this.activity = activity
        this.seriesChangedListener = seriesChangedListener

        seriesChanged(series)
    }

    override fun editTextLostFocus() {
        super.editTextLostFocus()

        val newSeriesTitle = getCurrentFieldValue()

        setValues(newSeriesTitle, newSeriesTitle)
        setDisplayedValue(false)
    }

    override fun hasFocusChanged(hasFocus: Boolean) {
        super.hasFocusChanged(hasFocus)

        if(hasFocus) {
            presenter.searchSeries(presenter.getLastSearchTerm())
        }
    }

    override fun enteredTextChanged(enteredText: String) {
        if(series == null && enteredText.isNotBlank()) { // user entered a title, but series is null -> create a new Series
            seriesChanged(Series(enteredText))
        }

        super.enteredTextChanged(enteredText)
    }

    override fun updateValueToEdit() {
        super.updateValueToEdit()

        if(edtxtEntityFieldValue.hasFocus()) {
            presenter.searchSeries(getCurrentFieldValue())
        }
    }


    override fun editDetails() {
        // currently not used as mnEditDetails is hidden (showEditDetailsMenuItem set to false)
        activity?.setWaitingForResult(EditSeriesActivity.ResultId)

        router.showEditSourceSeriesView(Source(""), series)
    }

    override fun createNewEntity() {
        seriesChanged(Series(""))

        startEditing()
    }

    override fun removeEntity() {
        seriesChanged(null)

        stopEditing()
    }

    override fun shareUrl() {
        // nothing to do here, menu item isn't shown for Series
    }

    private fun existingSeriesSelected(series: Series) {
        seriesChanged(series)

        hideSearchResultsView()
    }

    fun editingSeriesDone(result: EditSeriesActivityResult) {
        if(result.didSaveSeries) {
            setFieldValueForCurrentSeriesOnUiThread()
        }
        else if(result.didDeleteSeries) {
            removeEntity()
        }
    }

    fun seriesChanged(series: Series?) {
        this.series = series

        existingSeriesSearchResultsAdapter.selectedSeries = series

        setFieldValueForCurrentSeriesOnUiThread()

        seriesChangedListener?.invoke(series)
    }

    private fun setFieldValueForCurrentSeriesOnUiThread() {
        val displayedTitle = series?.title ?: ""
        setFieldValueOnUiThread(displayedTitle, displayedTitle)
    }


    /*      ISeriesListView implementation       */

    override fun showEntities(entities: List<Series>) {
        activity?.runOnUiThread {
            existingSeriesSearchResultsAdapter.items = entities

            if(entities.isEmpty() || edtxtEntityFieldValue.hasFocus() == false) {
                hideSearchResultsView()
            }
            else {
                showSearchResultsView()
            }
        }
    }

}
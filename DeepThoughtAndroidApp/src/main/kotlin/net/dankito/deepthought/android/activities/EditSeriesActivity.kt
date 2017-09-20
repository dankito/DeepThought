package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_edit_series.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityResult
import net.dankito.deepthought.android.adapter.SeriesOnReferenceRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.ActionItemUtil
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditSeriesPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.messages.SeriesChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class EditSeriesActivity : BaseActivity() {

    companion object {
        private const val SERIES_ID_BUNDLE_EXTRA_NAME = "SERIES_ID"
        private const val SERIES_TITLE_BUNDLE_EXTRA_NAME = "SERIES_TITLE"

        const val ResultId = "EDIT_SERIES_ACTIVITY_RESULT"
    }


    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var threadPool: IThreadPool

    @Inject
    protected lateinit var eventBus: IEventBus


    private var series: Series? = null

    private val presenter: EditSeriesPresenter

    private val existingSeriesSearchResultsAdapter: SeriesOnReferenceRecyclerAdapter

    private var didSeriesChange = false

    private var mnSaveSeries: MenuItem? = null


    private val actionItemHelper = ActionItemUtil()

    private var eventBusListener: EventBusListener? = null



    init {
        AppComponent.component.inject(this)

        presenter = EditSeriesPresenter(searchEngine, router, deleteEntityService, seriesService, threadPool)

        existingSeriesSearchResultsAdapter = SeriesOnReferenceRecyclerAdapter(presenter)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        showParameters(getParameters() as? EditSeriesActivityParameters)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(SERIES_ID_BUNDLE_EXTRA_NAME)?.let { seriesId -> showSeries(seriesId) }

        savedInstanceState.getString(SERIES_TITLE_BUNDLE_EXTRA_NAME)?.let { edtxtTitle.setText(it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(SERIES_ID_BUNDLE_EXTRA_NAME, series?.id)

            outState.putString(SERIES_TITLE_BUNDLE_EXTRA_NAME, edtxtTitle.text.toString())
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_series)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        edtxtTitle.addTextChangedListener(edtxtTitleTextWatcher)

        setupFindExistingSeriesSection()
    }

    private fun setupFindExistingSeriesSection() {
        btnCreateNewSeries.setOnClickListener { createSeries() } // TODO: check if previous series contains unsaved changes

        rcyExistingSeriesSearchResults.addItemDecoration(HorizontalDividerItemDecoration(this))
        rcyExistingSeriesSearchResults.adapter = existingSeriesSearchResultsAdapter
        existingSeriesSearchResultsAdapter.itemClickListener = { item -> existingSeriesSelected(item) }

        edtxtFindSeries.addTextChangedListener(edtxtFindSeriesTextWatcher)
    }


    override fun onDestroy() {
        unregisterEventBusListener()

        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_series_menu, menu)

        mnSaveSeries = menu.findItem(R.id.mnSaveSeries)
        mnSaveSeries?.isVisible = didSeriesChange

        actionItemHelper.setupLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                closeDialog()
                return true
            }
            R.id.mnSaveSeries -> {
                saveSeriesAndCloseDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }



    private fun saveSeriesAndCloseDialog() {
        unregisterEventBusListener()

        saveSeriesAsync { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
            else {
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveSeriesAsync(callback: (Boolean) -> Unit) {
        series?.let { series ->
            series.title = edtxtTitle.text.toString()

            presenter.saveSeriesAsync(series) { successful ->
                if(successful) {
                    setActivityResult(EditSeriesActivityResult(didSaveSeries = true, savedSeries = series))
                }
                callback(successful)
            }
        }
    }

    private fun setActivityResult(result: EditSeriesActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    private fun closeDialog() {
        finish()
    }


    private fun showParameters(parameters: EditSeriesActivityParameters?) {
        parameters?.let {
            if(parameters.series != null) {
                showSeries(parameters.series)
            }
            else {
                createSeries()
            }

            if(parameters.forReference != null) {
                lytSetReferenceSeriesControls.visibility = View.VISIBLE
            }
        }
    }

    private fun createSeries() {
        showSeries(Series(""))
    }

    private fun showSeries(seriesId: String) {
        seriesService.retrieve(seriesId)?.let { series ->
            showSeries(series)
        }
    }

    private fun showSeries(series: Series) {
        this.series = series
        existingSeriesSearchResultsAdapter.selectedSeries = series

        edtxtTitle.removeTextChangedListener(edtxtTitleTextWatcher)
        edtxtTitle.setText(series.title)
        edtxtTitle.addTextChangedListener(edtxtTitleTextWatcher)

        unregisterEventBusListener()
        mayRegisterEventBusListener()
    }


    private fun mayRegisterEventBusListener() {
        if(series?.isPersisted() ?: false) {
            synchronized(this) {
                val eventBusListenerInit = EventBusListener()

                eventBus.register(eventBusListenerInit)

                this.eventBusListener = eventBusListenerInit
            }
        }
    }

    private fun unregisterEventBusListener() {
        synchronized(this) {
            eventBusListener?.let {
                eventBus.unregister(it)
            }

            this.eventBusListener = null
        }
    }

    private fun seriesHasBeenEdited(series: Series) {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_series_alert_message_series_has_been_edited))
        }
    }


    private fun searchExistingSeries(query: String) {
        if(query.isNullOrBlank()) {
            hideRecyclerViewExistingSeriesSearchResults()
        }
        else {
            presenter.searchSeries(query) {
                runOnUiThread { retrievedExistingSeriesSearchResultsOnUiThread(it) }
            }
        }
    }

    private fun retrievedExistingSeriesSearchResultsOnUiThread(searchResults: List<Series>) {
        existingSeriesSearchResultsAdapter.items = searchResults

        showRecyclerViewExistingSeriesSearchResults()
    }

    private fun existingSeriesSelected(series: Series) {
        edtxtFindSeries.hideKeyboard()

        hideRecyclerViewExistingSeriesSearchResults()

        // TODO: check if previous series has unsaved changes
        showSeries(series)

        updateDidSeriesChangeOnUiThread(true)
    }

    private fun showRecyclerViewExistingSeriesSearchResults() {
        rcyExistingSeriesSearchResults.visibility = View.VISIBLE
        scrEditSeries.visibility = View.GONE

        (lytSetReferenceSeriesControls.layoutParams as? RelativeLayout.LayoutParams)?.let { layoutParams ->
            layoutParams.addRule(RelativeLayout.ABOVE, toolbar.id)
        }
    }

    private fun hideRecyclerViewExistingSeriesSearchResults() {
        rcyExistingSeriesSearchResults.visibility = View.GONE
        scrEditSeries.visibility = View.VISIBLE

        (lytSetReferenceSeriesControls.layoutParams as? RelativeLayout.LayoutParams)?.let { layoutParams ->
            layoutParams.addRule(RelativeLayout.ABOVE, 0)
        }
    }

    private val edtxtFindSeriesTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            searchExistingSeries(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }


    private val edtxtTitleTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            updateDidSeriesChangeOnUiThread(true)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

    }

    private fun updateDidSeriesChangeOnUiThread(didSeriesChange: Boolean) {
        this.didSeriesChange = didSeriesChange

        mnSaveSeries?.isVisible = didSeriesChange
    }


    inner class EventBusListener {

        @Handler
        fun entryChanged(change: SeriesChanged) {
            if(change.entity == series) {
                seriesHasBeenEdited(change.entity)
            }
        }
    }

}

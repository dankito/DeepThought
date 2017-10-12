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
import net.dankito.deepthought.android.views.ToolbarUtil
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditSeriesPresenter
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.ui.view.ISeriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.SeriesChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.IThreadPool
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class EditSeriesActivity : BaseActivity(), ISeriesListView {

    companion object {
        private const val SERIES_ID_BUNDLE_EXTRA_NAME = "SERIES_ID"
        private const val REFERENCE_TO_SET_SERIES_ON_BUNDLE_EXTRA_NAME = "REFERENCE_ID"
        private const val SELECTED_ANOTHER_SERIES_BUNDLE_EXTRA_NAME = "SELECTED_ANOTHER_SERIES"
        private const val DID_SERIES_CHANGE_BUNDLE_EXTRA_NAME = "DID_SERIES_CHANGE"

        const val ResultId = "EDIT_SERIES_ACTIVITY_RESULT"
    }


    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var seriesPersister: SeriesPersister

    @Inject
    protected lateinit var serializer: ISerializer

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

    private var referenceToSetSeriesOn: Reference? = null

    private val presenter: EditSeriesPresenter

    private val existingSeriesSearchResultsAdapter: SeriesOnReferenceRecyclerAdapter

    private var selectedAnotherSeries = false

    private var didSeriesChange = false

    private var mnSaveSeries: MenuItem? = null


    private val toolbarUtil = ToolbarUtil()

    private var eventBusListener: EventBusListener? = null



    init {
        AppComponent.component.inject(this)

        presenter = EditSeriesPresenter(this, searchEngine, router, deleteEntityService, seriesPersister, threadPool)

        existingSeriesSearchResultsAdapter = SeriesOnReferenceRecyclerAdapter(presenter)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        showParameters(getParameters() as? EditSeriesActivityParameters)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(SERIES_ID_BUNDLE_EXTRA_NAME, series?.id)

            referenceToSetSeriesOn?.let { outState.putString(REFERENCE_TO_SET_SERIES_ON_BUNDLE_EXTRA_NAME, serializer.serializeObject(it)) }

            outState.putBoolean(SELECTED_ANOTHER_SERIES_BUNDLE_EXTRA_NAME, selectedAnotherSeries)
            outState.putBoolean(DID_SERIES_CHANGE_BUNDLE_EXTRA_NAME, didSeriesChange)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState?.let {
            savedInstanceState.getString(SERIES_ID_BUNDLE_EXTRA_NAME)?.let { seriesId -> showSeries(seriesId) }

            savedInstanceState.getString(REFERENCE_TO_SET_SERIES_ON_BUNDLE_EXTRA_NAME)?.let { setReferenceToSetSeriesOn(serializer.deserializeObject(it, Reference::class.java)) }

            selectedAnotherSeries = savedInstanceState.getBoolean(SELECTED_ANOTHER_SERIES_BUNDLE_EXTRA_NAME, false)
            savedInstanceState.getBoolean(DID_SERIES_CHANGE_BUNDLE_EXTRA_NAME)?.let { didSeriesChange -> updateDidSeriesChangeOnUiThread(didSeriesChange) }
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_series)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lytEditSeriesTitle.setFieldNameOnUiThread(R.string.activity_edit_series_title_label) { updateDidSeriesChangeOnUiThread(it) }

        setupFindExistingSeriesSection()
    }

    private fun setupFindExistingSeriesSection() {
        btnCreateNewSeries.setOnClickListener { askIfANewSeriesShouldBeCreated() }

        rcyExistingSeriesSearchResults.addItemDecoration(HorizontalDividerItemDecoration(this))
        rcyExistingSeriesSearchResults.adapter = existingSeriesSearchResultsAdapter
        existingSeriesSearchResultsAdapter.itemClickListener = { item -> existingSeriesSelected(item) }
    }


    override fun onResume() {
        super.onResume()

        edtxtFindSeries.removeTextChangedListener(edtxtFindSeriesTextWatcher) // don't add edtxtFindSeriesTextWatcher twice
        edtxtFindSeries.addTextChangedListener(edtxtFindSeriesTextWatcher) // add edtxtFindSeriesTextWatcher here not in setupUI() as when restoring state edtxtFindSeries's text gets restored and edtxtFindSeriesTextWatcher therefore called
    }

    override fun onDestroy() {
        unregisterEventBusListener()

        presenter.cleanUp()

        super.onDestroy()
    }


    override fun onBackPressed() {
        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_series_menu, menu)

        mnSaveSeries = menu.findItem(R.id.mnSaveSeries)
        mnSaveSeries?.isVisible = didSeriesChange

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                return true
            }
            R.id.mnSaveSeries -> {
                saveSeriesAndCloseDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(didSeriesChange) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_series_alert_message_series_has_been_edited)) { shouldChangesGetSaved ->
            runOnUiThread {
                if(shouldChangesGetSaved) {
                    saveSeriesAndCloseDialog()
                }
                else {
                    closeDialog()
                }
            }
        }
    }


    private fun saveSeriesAndCloseDialog() {
        unregisterEventBusListener()

        if(didSeriesValuesChange()) {
            saveSeriesAsync { successful ->
                if(successful) {
                    runOnUiThread { closeDialog() }
                }
                else {
                    mayRegisterEventBusListener()
                }
            }
        }
        else {
            if(selectedAnotherSeries) {
                setActivityResult(EditSeriesActivityResult(didSaveSeries = true, savedSeries = series))
            }

            closeDialog()
        }
    }

    private fun didSeriesValuesChange(): Boolean {
        return series?.title != lytEditSeriesTitle.getCurrentFieldValue()
    }

    private fun saveSeriesAsync(callback: (Boolean) -> Unit) {
        series?.let { series ->
            series.title = lytEditSeriesTitle.getCurrentFieldValue()

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
                setReferenceToSetSeriesOn(parameters.forReference)
            }
        }
    }

    private fun setReferenceToSetSeriesOn(reference: Reference) {
        referenceToSetSeriesOn = reference
        lytSetReferenceSeriesControls.visibility = View.VISIBLE
    }

    private fun askIfANewSeriesShouldBeCreated() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_series_alert_message_create_new_series)) { createNewSeries ->
            if(createNewSeries) {
                if(didSeriesChange) {
                    askIfCurrentSeriesChangesShouldGetSaved()
                }
                else {
                    createSeries()
                }
            }
        }
    }

    private fun askIfCurrentSeriesChangesShouldGetSaved() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_series_alert_message_create_new_series_current_one_has_unsaved_changes)) { saveChanges ->
            if(saveChanges) {
                saveSeriesAsync { // TODO: show error message in case of failure
                    runOnUiThread { createSeries() }
                }
            }
            else {
                createSeries()
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

        lytEditSeriesTitle.setFieldValueOnUiThread(series.title)

        unregisterEventBusListener()
        mayRegisterEventBusListener()
    }


    private fun mayRegisterEventBusListener() {
        if(series?.isPersisted() ?: false && eventBusListener == null) {
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

    private fun warnSeriesHasBeenEdited(series: Series) {
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
                runOnUiThread { showRecyclerViewExistingSeriesSearchResults() }
            }
        }
    }

    private fun retrievedExistingSeriesSearchResultsOnUiThread(searchResults: List<Series>) {
        existingSeriesSearchResultsAdapter.items = searchResults
    }

    private fun existingSeriesSelected(series: Series) {
        edtxtFindSeries.hideKeyboard()

        hideRecyclerViewExistingSeriesSearchResults()

        selectedAnotherSeries = series?.id != this.series?.id

        // TODO: check if previous series has unsaved changes
        showSeries(series)

        updateDidSeriesChangeOnUiThread(true)
        mayRegisterEventBusListener()
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


    private fun updateDidSeriesChangeOnUiThread(didSeriesChange: Boolean) {
        this.didSeriesChange = didSeriesChange

        mnSaveSeries?.isVisible = didSeriesChange
    }


    /*     ISeriesListView implementation      */
    
    override fun showEntities(entities: List<Series>) {
        runOnUiThread {
            retrievedExistingSeriesSearchResultsOnUiThread(entities)
        }
    }


    inner class EventBusListener {

        @Handler
        fun entryChanged(change: SeriesChanged) {
            if(change.entity.id == series?.id) {
                if(change.source == EntityChangeSource.Local && (change.changeType == EntityChangeType.PreDelete || change.changeType == EntityChangeType.Deleted)) {
                    setActivityResult(EditSeriesActivityResult(didDeleteSeries = true))
                    runOnUiThread { closeDialog() }
                }
                else if(change.source == EntityChangeSource.Synchronization) {
                    warnSeriesHasBeenEdited(change.entity)
                }
            }
        }
    }

}

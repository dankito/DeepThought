package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_series.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityResult
import net.dankito.deepthought.android.adapter.SeriesOnSourceRecyclerAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.utils.android.ui.view.ToolbarUtil
import net.dankito.deepthought.data.SeriesPersister
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditSeriesPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SourceService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.SeriesChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import net.engio.mbassy.listener.Handler
import javax.inject.Inject


class EditSeriesActivity : BaseActivity() {

    companion object {
        private const val SERIES_ID_BUNDLE_EXTRA_NAME = "SERIES_ID"
        private const val DID_SERIES_CHANGE_BUNDLE_EXTRA_NAME = "DID_SERIES_CHANGE"

        const val ResultId = "EDIT_SERIES_ACTIVITY_RESULT"
    }


    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var sourceService: SourceService

    @Inject
    protected lateinit var seriesPersister: SeriesPersister

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

    private val existingSeriesSearchResultsAdapter: SeriesOnSourceRecyclerAdapter

    private var didSeriesChange = false

    private var mnSaveSeries: MenuItem? = null


    private val toolbarUtil = ToolbarUtil()

    private var eventBusListener: EventBusListener? = null



    init {
        AppComponent.component.inject(this)

        presenter = EditSeriesPresenter(router, deleteEntityService, seriesPersister, threadPool)

        existingSeriesSearchResultsAdapter = SeriesOnSourceRecyclerAdapter(presenter)
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

            outState.putBoolean(DID_SERIES_CHANGE_BUNDLE_EXTRA_NAME, didSeriesChange)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            savedInstanceState.getString(SERIES_ID_BUNDLE_EXTRA_NAME)?.let { seriesId -> showSeries(seriesId) }

            savedInstanceState.getBoolean(DID_SERIES_CHANGE_BUNDLE_EXTRA_NAME)?.let { didSeriesChange -> updateDidSeriesChangeOnUiThread(didSeriesChange) }
        }

        super.onRestoreInstanceState(savedInstanceState) // important: Call super method after restoring series so that all EditEntityFields with their modified values don't get overwritten by original series' values
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_series)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lytEditSeriesTitle.setFieldNameOnUiThread(R.string.activity_edit_series_title_label) { updateDidSeriesChangeOnUiThread(it) }
    }


    override fun onDestroy() {
        unregisterEventBusListener()

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
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_save))
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_series_alert_message_series_has_been_edited), config = config) { selectedButton ->
            runOnUiThread {
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    saveSeriesAndCloseDialog()
                }
                else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
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
        }
    }

    private fun askIfANewSeriesShouldBeCreated() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_series_alert_message_create_new_series)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
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
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_series_alert_message_create_new_series_current_one_has_unsaved_changes)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
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


    private fun updateDidSeriesChangeOnUiThread(didSeriesChange: Boolean) {
        this.didSeriesChange = didSeriesChange

        mnSaveSeries?.isVisible = didSeriesChange
    }


    inner class EventBusListener {

        @Handler
        fun seriesChanged(change: SeriesChanged) {
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

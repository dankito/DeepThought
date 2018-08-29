package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_source.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityResult
import net.dankito.deepthought.android.activities.arguments.EditSourceActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditSourceActivityResult
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.PickDateDialog
import net.dankito.deepthought.android.views.ToolbarUtil
import net.dankito.deepthought.data.SourcePersister
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.fields.SourceField
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditSourcePresenter
import net.dankito.utils.android.permissions.IPermissionsService
import net.dankito.utils.android.permissions.PermissionsService
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.SourceService
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.SourceChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import net.engio.mbassy.listener.Handler
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashSet


class EditSourceActivity : BaseActivity() {

    companion object {
        private const val SOURCE_ID_BUNDLE_EXTRA_NAME = "SOURCE_ID"
        private const val UNPERSISTED_SOURCE_BUNDLE_EXTRA_NAME = "UNPERSISTED_SOURCE_ID"
        private const val SOURCE_SERIES_ID_BUNDLE_EXTRA_NAME = "SOURCE_SERIES_ID"
        private const val ORIGINALLY_SET_SOURCE_SERIES_ID_BUNDLE_EXTRA_NAME = "ORIGINALLY_SET_SOURCE_SERIES_ID"

        const val ResultId = "EDIT_SOURCE_ACTIVITY_RESULT"
    }



    @Inject
    protected lateinit var sourceService: SourceService

    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var sourcePersister: SourcePersister

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var eventBus: IEventBus


    private val presenter: EditSourcePresenter

    private var source: Source? = null

    private var originallySetSeries: Series? = null

    private var currentlySetSeries: Series? = null

    private var currentlySetPublishingDate: Date? = null


    private var didSourceChange = false

    private val changedFields = HashSet<SourceField>()

    private var mnSaveSource: MenuItem? = null

    private val toolbarUtil = ToolbarUtil()

    private val permissionsManager: IPermissionsService

    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditSourcePresenter(router, clipboardService, deleteEntityService, sourcePersister)

        permissionsManager = PermissionsService(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        showParameters(getParameters() as? EditSourceActivityParameters)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            source?.let { source ->
                if(source.id != null) {
                    outState.putString(SOURCE_ID_BUNDLE_EXTRA_NAME, source.id)
                }
                else {
                    serializeStateToDiskIfNotNull(outState, UNPERSISTED_SOURCE_BUNDLE_EXTRA_NAME, source)
                }
            }

            outState.putString(SOURCE_SERIES_ID_BUNDLE_EXTRA_NAME, currentlySetSeries?.id)

            outState.putString(ORIGINALLY_SET_SOURCE_SERIES_ID_BUNDLE_EXTRA_NAME, originallySetSeries?.id)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            savedInstanceState.getString(SOURCE_ID_BUNDLE_EXTRA_NAME)?.let { sourceId -> showSource(sourceId) }

            // TODO: also restore selected Series
            restoreStateFromDisk(savedInstanceState, UNPERSISTED_SOURCE_BUNDLE_EXTRA_NAME, Source::class.java)?.let { showSource(it) }

            savedInstanceState.getString(ORIGINALLY_SET_SOURCE_SERIES_ID_BUNDLE_EXTRA_NAME)?.let { originallySetSeriesId ->
                this.originallySetSeries = seriesService.retrieve(originallySetSeriesId)
                lytEditSourceSeries.setOriginalSeriesToEdit(originallySetSeries, this) { setSeriesToEdit(it) }
            }

            val seriesId = savedInstanceState.getString(SOURCE_SERIES_ID_BUNDLE_EXTRA_NAME)
            if(seriesId != null) {
                seriesService.retrieve(seriesId)?.let { series ->
                    lytEditSourceSeries.seriesChanged(series)
                }
            }
            else {
                lytEditSourceSeries.seriesChanged(null)
            }

            updateDidSourceChangeOnUiThread()
        }

        super.onRestoreInstanceState(savedInstanceState) // important: Call super method after restoring source so that all EditEntityFields with their modified values don't get overwritten by original source's values
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_source)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        lytEditSourceTitle.setFieldNameOnUiThread(R.string.activity_edit_source_title_label) { updateDidSourceChangeOnUiThread(SourceField.Title, it) }

        lytEditSourceSeries.didValueChangeListener = { didSeriesTitleChange -> updateDidSourceChangeOnUiThread(SourceField.SeriesTitle, didSeriesTitleChange) }

        lytEditSourceIssue.setFieldNameOnUiThread(R.string.activity_edit_source_issue_label) { updateDidSourceChangeOnUiThread(SourceField.Issue, it) }
        lytEditSourceLength.setFieldNameOnUiThread(R.string.activity_edit_source_length_label) { updateDidSourceChangeOnUiThread(SourceField.Length, it) }

        lytEditSourcePublishingDate.setFieldNameOnUiThread(R.string.activity_edit_source_publishing_date_label) { updateDidSourceChangeOnUiThread(SourceField.PublishingDate, it) }
        lytEditSourcePublishingDate.showActionIconOnUiThread(R.drawable.ic_date_range_white_48dp) { showDatePickerDialog() }
        lytEditSourcePublishingDate.fieldValueFocusChangedListener = { hasFocus ->
            if(hasFocus == false) {
                edtxtPublishingDateLostFocus(lytEditSourcePublishingDate.getCurrentFieldValue())
            }
        }

        lytEditSourceUrl.setFieldNameOnUiThread(R.string.activity_edit_source_url_label) { updateDidSourceChangeOnUiThread(SourceField.Url, it) }

        lytEditAttachedFiles.didValueChangeListener = { updateDidSourceChangeOnUiThread(SourceField.Files, it) }
    }

    private fun setSeriesToEdit(series: Series?) {
        this.currentlySetSeries = series

        updateDidSourceChangeOnUiThread(SourceField.Series, series?.id != originallySetSeries?.id)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditSeriesActivity.ResultId) as? EditSeriesActivityResult)?.let { result ->
            lytEditSourceSeries.editingSeriesDone(result)
        }

        (supportFragmentManager.findFragmentByTag(PickDateDialog.TAG) as? PickDateDialog)?.let { dialog ->
            dialog.restoreDialog { selectedPublishingDate(it) }
        }

        mayRegisterEventBusListener()
        lytEditSourceSeries.viewBecomesVisible()
        lytEditAttachedFiles.viewBecomesVisible()
    }

    override fun onPause() {
        unregisterEventBusListener()
        lytEditSourceSeries.viewGetsHidden()
        lytEditAttachedFiles.viewGetsHidden()

        super.onPause()
    }

    override fun onDestroy() {
        parameterHolder.clearActivityResults(EditSeriesActivity.ResultId)

        super.onDestroy()
    }


    override fun onBackPressed() {
        if(lytEditSourceSeries.handlesBackButtonPress()) {
            return
        }

        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_source_menu, menu)

        mnSaveSource = menu.findItem(R.id.mnSaveSource)
        setMenuItemSaveSourceVisibility()

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        return true
    }

    private fun setMenuItemSaveSourceVisibility() {
        mnSaveSource?.isVisible = didSourceChange || source?.isPersisted() == false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                return true
            }
            R.id.mnSaveSource -> {
                saveSourceAndCloseDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(didSourceChange) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        val config = ConfirmationDialogConfig(true, getString(R.string.action_cancel), true, getString(R.string.action_dismiss), getString(R.string.action_save))
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_source_alert_message_source_contains_unsaved_changes), config = config) { selectedButton ->
            runOnUiThread {
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    saveSourceAndCloseDialog()
                }
                else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                    closeDialog()
                }
            }
        }
    }

    private fun saveSourceAndCloseDialog() {
        unregisterEventBusListener()

        if(lytEditSourcePublishingDate.hasFocus()) { // OnFocusChangeListener doesn't get called when save action gets pressed
            edtxtPublishingDateLostFocus(lytEditSourcePublishingDate.getCurrentFieldValue())
        }

        saveSourceAsync { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
            else {
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveSourceAsync(callback: (Boolean) -> Unit) {
        source?.let { source ->
            if(changedFields.contains(SourceField.SeriesTitle)) {
                currentlySetSeries?.title = lytEditSourceSeries.getEditedValue() ?: ""
            }

            source.title = lytEditSourceTitle.getCurrentFieldValue()
            source.issue = if(lytEditSourceIssue.getCurrentFieldValue().isNullOrBlank()) null else lytEditSourceIssue.getCurrentFieldValue()
            source.length = if(lytEditSourceLength.getCurrentFieldValue().isNullOrBlank()) null else lytEditSourceLength.getCurrentFieldValue()
            source.url = if(lytEditSourceUrl.getCurrentFieldValue().isNullOrBlank()) null else lytEditSourceUrl.getCurrentFieldValue()

            presenter.saveSourceAsync(source, currentlySetSeries, currentlySetPublishingDate, lytEditSourcePublishingDate.getCurrentFieldValue(),
                    lytEditAttachedFiles.getEditedFiles()) { successful ->
                if(successful) {
                    setActivityResult(EditSourceActivityResult(didSaveSource = true, savedSource = source))
                }
                callback(successful)
            }
        }
    }

    private fun setActivityResult(result: EditSourceActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    private fun closeDialog() {
        finish()
    }


    private fun edtxtPublishingDateLostFocus(enteredText: String) {
        val previousValue = this.currentlySetPublishingDate

        this.currentlySetPublishingDate = presenter.parsePublishingDate(enteredText)

        updateDidSourceChangeOnUiThread(SourceField.PublishingDate, currentlySetPublishingDate != previousValue)

        // TODO: show hint or error when publishingDateString couldn't be parsed?
//        if(currentlySetPublishingDate == null) {
//            edtxtPublishingDate.error = getString(R.string.activity_edit_source_error_could_not_parse_publishing_date, enteredText)
//        }
    }

    private fun showDatePickerDialog() {
        val pickDateDialog = PickDateDialog()

        pickDateDialog.show(supportFragmentManager, currentlySetPublishingDate) { selectedDate ->
            selectedPublishingDate(selectedDate)
        }
    }

    private fun selectedPublishingDate(selectedDate: Date) {
        showPublishingDate(selectedDate)
        updateDidSourceChangeOnUiThread(SourceField.PublishingDate, true)
    }


    private fun showParameters(parameters: EditSourceActivityParameters?) {
        parameters?.let {
            if(parameters.source != null) {
                this.originallySetSeries = parameters.series ?: parameters.source.series

                showSource(parameters.source, parameters.series, parameters.editedSourceTitle)
            }
            else {
                createSource()
            }
        }
    }

    private fun createSource() {
        showSource(Source())
    }

    private fun showSource(sourceId: String) {
        sourceService.retrieve(sourceId)?.let { source ->
            showSource(source)
        }
    }

    private fun showSource(source: Source, series: Series? = null, editedSourceTitle: String? = null) {
        this.source = source

        lytEditSourceTitle.setFieldValueOnUiThread(editedSourceTitle ?: source.title)

        lytEditSourceSeries.setOriginalSeriesToEdit(series ?: source.series, this) { setSeriesToEdit(it) }

        lytEditSourceIssue.setFieldValueOnUiThread(source.issue ?: "")
        lytEditSourceLength.setFieldValueOnUiThread(source.length ?: "")
        showPublishingDate(source.publishingDate, source.publishingDateString)

        lytEditSourceUrl.setFieldValueOnUiThread(source.url ?: "")

        lytEditAttachedFiles.setFiles(source.attachedFiles, permissionsManager, source)

        unregisterEventBusListener() // TODO: why? i came here from showParameters() and then we need to listen to changes to Source
        updateDidSourceChangeOnUiThread()
    }

    private fun showPublishingDate(publishingDate: Date?, publishingDateString: String? = null) {
        this.currentlySetPublishingDate = publishingDate

        val publishingDateStringToShow =
        if(publishingDateString != null) {
            publishingDateString
        }
        else if(publishingDate != null) {
            presenter.convertPublishingDateToText(publishingDate)
        }
        else {
            ""
        }

        lytEditSourcePublishingDate.setFieldValueOnUiThread(publishingDateStringToShow)
    }


    private fun updateDidSourceChangeOnUiThread(field: SourceField, didFieldValueChange: Boolean) {
        if(didFieldValueChange) {
            changedFields.add(field)
        }
        else {
            changedFields.remove(field)
        }

        updateDidSourceChangeOnUiThread()
    }

    private fun updateDidSourceChangeOnUiThread() {
        this.didSourceChange = changedFields.size > 0

        setMenuItemSaveSourceVisibility()
    }


    private fun mayRegisterEventBusListener() {
        if(source?.isPersisted() == true && eventBusListener == null) {
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

    private fun warnSourceHasBeenEdited(source: Source) {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_source_alert_message_source_has_been_edited))
        }
    }


    inner class EventBusListener {

        @Handler
        fun sourceChanged(change: SourceChanged) {
            if(change.entity.id == source?.id && change.isDependentChange == false) {
                if(change.source == EntityChangeSource.Local && (change.changeType == EntityChangeType.PreDelete || change.changeType == EntityChangeType.Deleted)) {
                    setActivityResult(EditSourceActivityResult(didDeleteSource = true))
                    runOnUiThread { closeDialog() }
                }
                else if(change.source == EntityChangeSource.Synchronization) {
                    warnSourceHasBeenEdited(change.entity)
                }
            }
        }
    }

}
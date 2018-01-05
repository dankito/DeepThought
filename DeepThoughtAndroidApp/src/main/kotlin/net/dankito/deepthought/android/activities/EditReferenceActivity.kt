package net.dankito.deepthought.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_edit_reference.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityResult
import net.dankito.deepthought.android.activities.arguments.ViewPdfActivityParameters
import net.dankito.deepthought.android.adapter.FilesAdapter
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.PickDateDialog
import net.dankito.deepthought.android.views.ToolbarUtil
import net.dankito.deepthought.data.ReferencePersister
import net.dankito.deepthought.model.FileLink
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.fields.SourceField
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditReferencePresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.SeriesService
import net.dankito.service.data.messages.EntityChangeSource
import net.dankito.service.data.messages.EntityChangeType
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import net.engio.mbassy.listener.Handler
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashSet


class EditReferenceActivity : BaseActivity() {

    companion object {
        private const val REFERENCE_ID_BUNDLE_EXTRA_NAME = "REFERENCE_ID"
        private const val UNPERSISTED_REFERENCE_BUNDLE_EXTRA_NAME = "UNPERSISTED_REFERENCE_ID"
        private const val REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME = "REFERENCE_SERIES_ID"
        private const val ORIGINALLY_SET_REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME = "ORIGINALLY_SET_REFERENCE_SERIES_ID"

        const val ResultId = "EDIT_REFERENCE_ACTIVITY_RESULT"
    }



    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var referencePersister: ReferencePersister

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


    private val presenter: EditReferencePresenter

    private var source: Source? = null

    private var originallySetSeries: Series? = null

    private var currentlySetSeries: Series? = null

    private var currentlySetPublishingDate: Date? = null


    private val attachedFilesAdapter = FilesAdapter()

    private var didReferenceChange = false

    private val changedFields = HashSet<SourceField>()

    private var mnSaveReference: MenuItem? = null

    private val toolbarUtil = ToolbarUtil()

    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditReferencePresenter(router, clipboardService, deleteEntityService, referencePersister)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        showParameters(getParameters() as? EditReferenceActivityParameters)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            source?.let { reference ->
                if(reference.id != null) {
                    outState.putString(REFERENCE_ID_BUNDLE_EXTRA_NAME, reference.id)
                }
                else {
                    outState.putString(UNPERSISTED_REFERENCE_BUNDLE_EXTRA_NAME, serializer.serializeObject(reference))
                }
            }

            outState.putString(REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME, currentlySetSeries?.id)

            outState.putString(ORIGINALLY_SET_REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME, originallySetSeries?.id)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            savedInstanceState.getString(REFERENCE_ID_BUNDLE_EXTRA_NAME)?.let { referenceId -> showReference(referenceId) }

            // TODO: also restore selected Series
            savedInstanceState.getString(UNPERSISTED_REFERENCE_BUNDLE_EXTRA_NAME)?.let { showReference(serializer.deserializeObject(it, Source::class.java)) }

            savedInstanceState.getString(ORIGINALLY_SET_REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME)?.let { originallySetSeriesId ->
                this.originallySetSeries = seriesService.retrieve(originallySetSeriesId)
                lytEditReferenceSeries.setOriginalSeriesToEdit(originallySetSeries, this) { setSeriesToEdit(it) }
            }

            val seriesId = savedInstanceState.getString(REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME)
            if(seriesId != null) {
                seriesService.retrieve(seriesId)?.let { series ->
                    lytEditReferenceSeries.seriesChanged(series)
                }
            }
            else {
                lytEditReferenceSeries.seriesChanged(null)
            }

            updateDidReferenceChangeOnUiThread()
        }

        super.onRestoreInstanceState(savedInstanceState) // important: Call super method after restoring reference so that all EditEntityFields with their modified values don't get overwritten by original reference's values
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_reference)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        lytEditReferenceTitle.setFieldNameOnUiThread(R.string.activity_edit_source_title_label) { updateDidReferenceChangeOnUiThread(SourceField.Title, it) }

        lytEditReferenceSeries.didValueChangeListener = { didSeriesTitleChange -> seriesTitleChanged(didSeriesTitleChange) }

        lytEditReferenceIssue.setFieldNameOnUiThread(R.string.activity_edit_source_issue_label) { updateDidReferenceChangeOnUiThread(SourceField.Issue, it) }
        lytEditReferenceLength.setFieldNameOnUiThread(R.string.activity_edit_source_length_label) { updateDidReferenceChangeOnUiThread(SourceField.Length, it) }

        lytEditReferencePublishingDate.setFieldNameOnUiThread(R.string.activity_edit_source_publishing_date_label) { updateDidReferenceChangeOnUiThread(SourceField.PublishingDate, it) }
        lytEditReferencePublishingDate.showActionIconOnUiThread(R.drawable.ic_date_range_white_48dp) { showDatePickerDialog() }
        lytEditReferencePublishingDate.fieldValueFocusChangedListener = { hasFocus ->
            if(hasFocus == false) {
                edtxtPublishingDateLostFocus(lytEditReferencePublishingDate.getCurrentFieldValue())
            }
        }

        lytEditReferenceUrl.setFieldNameOnUiThread(R.string.activity_edit_source_url_label) { updateDidReferenceChangeOnUiThread(SourceField.Url, it) }

        lstSourceAttachedFiles.adapter = attachedFilesAdapter
        lstSourceAttachedFiles.setOnItemClickListener { _, _, position, _ -> showFile(attachedFilesAdapter.getItem(position)) }
    }

    // TODO: move to Router
    private fun showFile(file: FileLink) {
        val intent = Intent(this, ViewPdfActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val parameters = ViewPdfActivityParameters(File(file.uriString), source)
        val id = parameterHolder.setParameters(parameters)

        intent.putExtra(BaseActivity.ParametersId, id)

        startActivity(intent)
    }

    private fun seriesTitleChanged(didSeriesTitleChange: Boolean) {
        updateDidReferenceChangeOnUiThread(SourceField.SeriesTitle, didSeriesTitleChange)
    }

    private fun setSeriesToEdit(series: Series?) {
        this.currentlySetSeries = series

        updateDidReferenceChangeOnUiThread(SourceField.Series, series?.id != originallySetSeries?.id)
    }


    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditSeriesActivity.ResultId) as? EditSeriesActivityResult)?.let { result ->
            lytEditReferenceSeries.editingSeriesDone(result)
        }

        (supportFragmentManager.findFragmentByTag(PickDateDialog.TAG) as? PickDateDialog)?.let { dialog ->
            dialog.restoreDialog { selectedPublishingDate(it) }
        }

        mayRegisterEventBusListener()
    }

    override fun onPause() {
        unregisterEventBusListener()

        super.onPause()
    }

    override fun onDestroy() {
        parameterHolder.clearActivityResults(EditSeriesActivity.ResultId)

        super.onDestroy()
    }


    override fun onBackPressed() {
        if(lytEditReferenceSeries.handlesBackButtonPress()) {
            return
        }

        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_reference_menu, menu)

        mnSaveReference = menu.findItem(R.id.mnSaveReference)
        mnSaveReference?.isVisible = didReferenceChange

        toolbarUtil.setupActionItemsLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                return true
            }
            R.id.mnSaveReference -> {
                saveReferenceAndCloseDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(didReferenceChange) {
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
                    saveReferenceAndCloseDialog()
                }
                else if(selectedButton == ConfirmationDialogButton.ThirdButton) {
                    closeDialog()
                }
            }
        }
    }

    private fun saveReferenceAndCloseDialog() {
        unregisterEventBusListener()

        if(lytEditReferencePublishingDate.hasFocus()) { // OnFocusChangeListener doesn't get called when save action gets pressed
            edtxtPublishingDateLostFocus(lytEditReferencePublishingDate.getCurrentFieldValue())
        }

        saveReferenceAsync { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
            else {
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveReferenceAsync(callback: (Boolean) -> Unit) {
        source?.let { reference ->
            if(changedFields.contains(SourceField.SeriesTitle)) {
                currentlySetSeries?.title = lytEditReferenceSeries.getEditedValue() ?: ""
            }

            reference.title = lytEditReferenceTitle.getCurrentFieldValue()
            reference.issue = if(lytEditReferenceIssue.getCurrentFieldValue().isNullOrBlank()) null else lytEditReferenceIssue.getCurrentFieldValue()
            reference.length = if(lytEditReferenceLength.getCurrentFieldValue().isNullOrBlank()) null else lytEditReferenceLength.getCurrentFieldValue()
            reference.url = if(lytEditReferenceUrl.getCurrentFieldValue().isNullOrBlank()) null else lytEditReferenceUrl.getCurrentFieldValue()

            presenter.saveReferenceAsync(reference, currentlySetSeries, currentlySetPublishingDate, lytEditReferencePublishingDate.getCurrentFieldValue()) { successful ->
                if(successful) {
                    setActivityResult(EditReferenceActivityResult(didSaveReference = true, savedSource = reference))
                }
                callback(successful)
            }
        }
    }

    private fun setActivityResult(result: EditReferenceActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    private fun closeDialog() {
        finish()
    }


    private fun edtxtPublishingDateLostFocus(enteredText: String) {
        val previousValue = this.currentlySetPublishingDate

        this.currentlySetPublishingDate = presenter.parsePublishingDate(enteredText)

        updateDidReferenceChangeOnUiThread(SourceField.PublishingDate, currentlySetPublishingDate != previousValue)

        // TODO: show hint or error when publishingDateString couldn't be parsed?
//        if(currentlySetPublishingDate == null) {
//            edtxtPublishingDate.error = getString(R.string.activity_edit_reference_error_could_not_parse_publishing_date, enteredText)
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
        updateDidReferenceChangeOnUiThread(SourceField.PublishingDate, true)
    }


    private fun showParameters(parameters: EditReferenceActivityParameters?) {
        parameters?.let {
            if(parameters.source != null) {
                this.originallySetSeries = parameters.series ?: parameters.source.series

                showReference(parameters.source, parameters.series, parameters.editedSourceTitle)
            }
            else {
                createReference()
            }
        }
    }

    private fun askIfANewReferenceShouldBeCreated() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_source_alert_message_create_new_source)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                if(didReferenceChange) {
                    askIfCurrentReferenceChangesShouldGetSaved()
                }
                else {
                    createReference()
                }
            }
        }
    }

    private fun askIfCurrentReferenceChangesShouldGetSaved() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_source_alert_message_create_new_source_current_one_has_unsaved_changes)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                saveReferenceAsync { // TODO: show error message in case of failure
                    runOnUiThread { createReference() }
                }
            }
            else {
                createReference()
            }
        }
    }

    private fun createReference() {
        showReference(Source())
    }

    private fun showReference(referenceId: String) {
        referenceService.retrieve(referenceId)?.let { reference ->
            showReference(reference)
        }
    }

    private fun showReference(source: Source, series: Series? = null, editedSourceTitle: String? = null) {
        this.source = source

        lytEditReferenceTitle.setFieldValueOnUiThread(editedSourceTitle ?: source.title)

        lytEditReferenceSeries.setOriginalSeriesToEdit(series ?: source.series, this) { setSeriesToEdit(it) }

        lytEditReferenceIssue.setFieldValueOnUiThread(source.issue ?: "")
        lytEditReferenceLength.setFieldValueOnUiThread(source.length ?: "")
        showPublishingDate(source.publishingDate, source.publishingDateString)

        lytEditReferenceUrl.setFieldValueOnUiThread(source.url ?: "")

        attachedFilesAdapter.setItems(source.attachedFiles)

        unregisterEventBusListener() // TODO: why? i came here from showParameters() and then we need to listen to changes to Source
        updateDidReferenceChangeOnUiThread()
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

        lytEditReferencePublishingDate.setFieldValueOnUiThread(publishingDateStringToShow)
    }


    private fun updateDidReferenceChangeOnUiThread(field: SourceField, didFieldValueChange: Boolean) {
        if(didFieldValueChange) {
            changedFields.add(field)
        }
        else {
            changedFields.remove(field)
        }

        updateDidReferenceChangeOnUiThread()
    }

    private fun updateDidReferenceChangeOnUiThread() {
        this.didReferenceChange = changedFields.size > 0 || source?.isPersisted() == false

        mnSaveReference?.isVisible = didReferenceChange
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

    private fun warnReferenceHasBeenEdited(source: Source) {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_source_alert_message_source_has_been_edited))
        }
    }


    inner class EventBusListener {

        @Handler
        fun entryChanged(change: ReferenceChanged) {
            if(change.entity.id == source?.id && change.isDependentChange == false) {
                if(change.source == EntityChangeSource.Local && (change.changeType == EntityChangeType.PreDelete || change.changeType == EntityChangeType.Deleted)) {
                    setActivityResult(EditReferenceActivityResult(didDeleteReference = true))
                    runOnUiThread { closeDialog() }
                }
                else if(change.source == EntityChangeSource.Synchronization) {
                    warnReferenceHasBeenEdited(change.entity)
                }
            }
        }
    }

}
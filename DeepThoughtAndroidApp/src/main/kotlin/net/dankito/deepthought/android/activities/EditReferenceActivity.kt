package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_edit_reference.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityParameters
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
import net.dankito.deepthought.android.activities.arguments.EditSeriesActivityResult
import net.dankito.deepthought.android.adapter.ReferenceOnEntryRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.PickDateDialog
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.ToolbarUtil
import net.dankito.deepthought.data.ReferencePersister
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
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashSet


class EditReferenceActivity : BaseActivity() {

    companion object {
        private const val REFERENCE_ID_BUNDLE_EXTRA_NAME = "REFERENCE_ID"
        private const val UNPERSISTED_REFERENCE_BUNDLE_EXTRA_NAME = "UNPERSISTED_REFERENCE_ID"
        private const val REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME = "REFERENCE_SERIES_ID"
        private const val ORIGINALLY_SET_REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME = "ORIGINALLY_SET_REFERENCE_SERIES_ID"
        private const val SELECTED_ANOTHER_REFERENCE_BUNDLE_EXTRA_NAME = "SELECTED_ANOTHER_REFERENCE"
        private const val IS_FOR_ENTRY_SET_BUNDLE_EXTRA_NAME = "IS_FOR_ENTRY_SET"

        const val ResultId = "EDIT_REFERENCE_ACTIVITY_RESULT"
    }



    @Inject
    protected lateinit var referenceService: ReferenceService

    @Inject
    protected lateinit var seriesService: SeriesService

    @Inject
    protected lateinit var referencePersister: ReferencePersister

    @Inject
    protected lateinit var searchEngine: ISearchEngine

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


    private val existingReferencesSearchResultsAdapter: ReferenceOnEntryRecyclerAdapter

    private var didReferenceChange = false

    private var selectedAnotherReference = false

    private val changedFields = HashSet<SourceField>()

    private var mnSaveReference: MenuItem? = null

    private val toolbarUtil = ToolbarUtil()

    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditReferencePresenter(searchEngine, router, clipboardService, deleteEntityService, referencePersister)

        existingReferencesSearchResultsAdapter = ReferenceOnEntryRecyclerAdapter(presenter)
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

            outState.putBoolean(SELECTED_ANOTHER_REFERENCE_BUNDLE_EXTRA_NAME, selectedAnotherReference)

            outState.putBoolean(IS_FOR_ENTRY_SET_BUNDLE_EXTRA_NAME, lytSetEntryReferenceControls.visibility == View.VISIBLE)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            savedInstanceState.getString(REFERENCE_ID_BUNDLE_EXTRA_NAME)?.let { referenceId -> showReference(referenceId) }

            savedInstanceState.getString(UNPERSISTED_REFERENCE_BUNDLE_EXTRA_NAME)?.let { showReference(serializer.deserializeObject(it, Source::class.java)) }

            savedInstanceState.getString(ORIGINALLY_SET_REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME)?.let { originallySetSeriesId ->
                this.originallySetSeries = seriesService.retrieve(originallySetSeriesId)
            }

            val seriesId = savedInstanceState.getString(REFERENCE_SERIES_ID_BUNDLE_EXTRA_NAME)
            if(seriesId != null) {
                setAndShowSeriesOnUiThread(seriesId)
            }
            else {
                setAndShowSeriesOnUiThread(null)
            }

            this.selectedAnotherReference = savedInstanceState.getBoolean(SELECTED_ANOTHER_REFERENCE_BUNDLE_EXTRA_NAME, false)
            updateDidReferenceChangeOnUiThread()

            if(savedInstanceState.getBoolean(IS_FOR_ENTRY_SET_BUNDLE_EXTRA_NAME, false) == true) {
                lytSetEntryReferenceControls.visibility = View.VISIBLE
            }
        }

        super.onRestoreInstanceState(savedInstanceState) // important: Call super method after restoring reference so that all EditEntityFields with their modified values don't get overwritten by original reference's values
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_reference)

        setSupportActionBar(toolbar)
        toolbarUtil.adjustToolbarLayoutDelayed(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupFindExistingReferenceSection()


        lytEditReferenceTitle.setFieldNameOnUiThread(R.string.activity_edit_reference_title_label) { updateDidReferenceChangeOnUiThread(SourceField.Title, it) }

        lytEditReferenceSeries.setFieldNameOnUiThread(R.string.activity_edit_reference_series_label, false)
        lytEditReferenceSeries.fieldClickedListener = { editSeries() }

        lytEditReferenceIssue.setFieldNameOnUiThread(R.string.activity_edit_reference_issue_label) { updateDidReferenceChangeOnUiThread(SourceField.Issue, it) }

        lytEditReferencePublishingDate.setFieldNameOnUiThread(R.string.activity_edit_reference_publishing_date_label) { updateDidReferenceChangeOnUiThread(SourceField.PublishingDate, it) }
        lytEditReferencePublishingDate.showActionIconOnUiThread(R.drawable.ic_date_range_white_48dp) { showDatePickerDialog() }
        lytEditReferencePublishingDate.fieldValueFocusChangedListener = { hasFocus ->
            if(hasFocus == false) {
                edtxtPublishingDateLostFocus(lytEditReferencePublishingDate.getCurrentFieldValue())
            }
        }

        lytEditReferenceUrl.setFieldNameOnUiThread(R.string.activity_edit_reference_url_label) { updateDidReferenceChangeOnUiThread(SourceField.Url, it) }
    }

    private fun setupFindExistingReferenceSection() {
        btnCreateNewReference.setOnClickListener { askIfANewReferenceShouldBeCreated() }

        rcyExistingReferencesSearchResults.addItemDecoration(HorizontalDividerItemDecoration(this))
        rcyExistingReferencesSearchResults.adapter = existingReferencesSearchResultsAdapter
        existingReferencesSearchResultsAdapter.itemClickListener = { item -> existingReferenceSelected(item) }

        edtxtFindReferences.addTextChangedListener(edtxtFindReferencesTextWatcher)
    }


    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditSeriesActivity.ResultId) as? EditSeriesActivityResult)?.let { result ->
            if(result.didSaveSeries) {
                result.savedSeries?.let { savedSeriesOnUiThread(it) }
            }
            if(result.didDeleteSeries) {
                setAndShowSeriesOnUiThread(null)
            }
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


    private fun editSeries() {
        source?.let {
            setWaitingForResult(EditSeriesActivity.ResultId)

            presenter.editSeries(it, currentlySetSeries)
        }
    }

    private fun savedSeriesOnUiThread(series: Series) {
        // do not set series directly on source as if source is not saved yet adding it to series.sources causes an error
        setAndShowSeriesOnUiThread(series)
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
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_reference_alert_message_reference_contains_unsaved_changes)) { shouldChangesGetSaved ->
            runOnUiThread {
                if(shouldChangesGetSaved) {
                    saveReferenceAndCloseDialog()
                }
                else {
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
            reference.title = lytEditReferenceTitle.getCurrentFieldValue()
            reference.issue = if(lytEditReferenceIssue.getCurrentFieldValue().isNullOrBlank()) null else lytEditReferenceIssue.getCurrentFieldValue()
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

                showReference(parameters.source, parameters.series)
            }
            else {
                createReference()
            }

            if(parameters.forItem != null) {
                lytSetEntryReferenceControls.visibility = View.VISIBLE
            }
        }
    }

    private fun askIfANewReferenceShouldBeCreated() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_reference_alert_message_create_new_reference)) { createNewReference ->
            if(createNewReference) {
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
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_reference_alert_message_create_new_reference_current_one_has_unsaved_changes)) { saveChanges ->
            if(saveChanges) {
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
        showReference(Source(""))
    }

    private fun showReference(referenceId: String) {
        referenceService.retrieve(referenceId)?.let { reference ->
            showReference(reference)
        }
    }

    private fun showReference(source: Source, series: Series? = null) {
        this.source = source
        existingReferencesSearchResultsAdapter.selectedSource = source

        lytEditReferenceTitle.setFieldValueOnUiThread(source.title)

        setAndShowSeriesOnUiThread(series ?: source.series)

        lytEditReferenceIssue.setFieldValueOnUiThread(source.issue ?: "")
        showPublishingDate(source.publishingDate, source.publishingDateString)

        lytEditReferenceUrl.setFieldValueOnUiThread(source.url ?: "")

        unregisterEventBusListener()
    }

    private fun setAndShowSeriesOnUiThread(seriesId: String) {
        seriesService.retrieve(seriesId)?.let { series ->
            setAndShowSeriesOnUiThread(series)
        }
    }

    private fun setAndShowSeriesOnUiThread(series: Series?) {
        this.currentlySetSeries = series

        if(series != null) {
            lytEditReferenceSeries.setFieldValueOnUiThread(series.title)
            lytEditReferenceSeries.showActionIconOnUiThread(android.R.drawable.ic_delete) {
                setAndShowSeriesOnUiThread(null)
            }
        }
        else {
            lytEditReferenceSeries.setFieldValueOnUiThread("")
            lytEditReferenceSeries.showActionIconOnUiThread(R.drawable.ic_search_white_48dp) { editSeries() }
        }

        updateDidReferenceChangeOnUiThread(SourceField.Series, series?.id != originallySetSeries?.id)
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
        this.didReferenceChange = changedFields.size > 0 || selectedAnotherReference

        mnSaveReference?.isVisible = didReferenceChange
    }


    private fun mayRegisterEventBusListener() {
        if(source?.isPersisted() ?: false && eventBusListener == null) {
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
            dialogService.showInfoMessage(getString(R.string.activity_edit_reference_alert_message_reference_has_been_edited))
        }
    }


    private fun searchExistingReferences(query: String) {
        if(query.isNullOrBlank()) {
            hideRecyclerViewExistingReferencesSearchResults()
        }
        else {
            searchEngine.searchReferences(ReferenceSearch(query) {
                runOnUiThread { retrievedExistingReferencesSearchResultsOnUiThread(it) }
            })
        }
    }

    private fun retrievedExistingReferencesSearchResultsOnUiThread(searchResults: List<Source>) {
        existingReferencesSearchResultsAdapter.items = searchResults

        showRecyclerViewExistingReferencesSearchResults()
    }

    private fun existingReferenceSelected(source: Source) {
        edtxtFindReferences.hideKeyboard()

        hideRecyclerViewExistingReferencesSearchResults()

        selectedAnotherReference = source?.id != this.source?.id

        // TODO: check if previous source has unsaved changes
        showReference(source)

        updateDidReferenceChangeOnUiThread()
        mayRegisterEventBusListener()
    }

    private fun showRecyclerViewExistingReferencesSearchResults() {
        rcyExistingReferencesSearchResults.visibility = View.VISIBLE
        scrEditReference.visibility = View.GONE

        (lytSetEntryReferenceControls.layoutParams as? RelativeLayout.LayoutParams)?.let { layoutParams ->
            layoutParams.addRule(RelativeLayout.ABOVE, toolbar.id)
        }
    }

    private fun hideRecyclerViewExistingReferencesSearchResults() {
        rcyExistingReferencesSearchResults.visibility = View.GONE
        scrEditReference.visibility = View.VISIBLE

        (lytSetEntryReferenceControls.layoutParams as? RelativeLayout.LayoutParams)?.let { layoutParams ->
            layoutParams.addRule(RelativeLayout.ABOVE, 0)
        }
    }

    private val edtxtFindReferencesTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            searchExistingReferences(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

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
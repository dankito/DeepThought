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
import net.dankito.deepthought.android.adapter.ReferenceOnEntryRecyclerAdapter
import net.dankito.deepthought.android.adapter.viewholder.HorizontalDividerItemDecoration
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.PickDateDialog
import net.dankito.deepthought.android.service.hideKeyboard
import net.dankito.deepthought.android.views.ActionItemUtil
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditReferencePresenter
import net.dankito.deepthought.ui.presenter.util.ReferencePersister
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.messages.ReferenceChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import java.util.*
import javax.inject.Inject


class EditReferenceActivity : BaseActivity() {

    companion object {
        private const val REFERENCE_ID_BUNDLE_EXTRA_NAME = "REFERENCE_ID"
        private const val REFERENCE_TITLE_BUNDLE_EXTRA_NAME = "REFERENCE_TITLE"
        private const val REFERENCE_SERIES_BUNDLE_EXTRA_NAME = "REFERENCE_SERIES"
        private const val REFERENCE_ISSUE_BUNDLE_EXTRA_NAME = "REFERENCE_ISSUE"
        private const val REFERENCE_PUBLISHING_DATE_BUNDLE_EXTRA_NAME = "REFERENCE_PUBLISHING_DATE"
        private const val REFERENCE_URL_BUNDLE_EXTRA_NAME = "REFERENCE_URL"

        const val ResultId = "EDIT_REFERENCE_ACTIVITY_RESULT"
    }



    @Inject
    protected lateinit var referenceService: ReferenceService

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

    private var reference: Reference? = null

    private var currentlySetPublishingDate: Date? = null


    private val existingReferencesSearchResultsAdapter: ReferenceOnEntryRecyclerAdapter

    private val actionItemHelper = ActionItemUtil()

    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditReferencePresenter(searchEngine, router, clipboardService, deleteEntityService, referencePersister)

        existingReferencesSearchResultsAdapter = ReferenceOnEntryRecyclerAdapter(presenter)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        showParameters(getParameters() as? EditReferenceActivityParameters)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(REFERENCE_ID_BUNDLE_EXTRA_NAME)?.let { referenceId -> showReference(referenceId) }

        savedInstanceState.getString(REFERENCE_TITLE_BUNDLE_EXTRA_NAME)?.let { edtxtTitle.setText(it) }
        savedInstanceState.getString(REFERENCE_SERIES_BUNDLE_EXTRA_NAME)?.let { edtxtSeries.setText(it) }
        savedInstanceState.getString(REFERENCE_ISSUE_BUNDLE_EXTRA_NAME)?.let { edtxtIssue.setText(it) }
        savedInstanceState.getString(REFERENCE_PUBLISHING_DATE_BUNDLE_EXTRA_NAME)?.let { edtxtPublishingDate.setText(it) }
        savedInstanceState.getString(REFERENCE_URL_BUNDLE_EXTRA_NAME)?.let { edtxtUrl.setText(it) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let {
            outState.putString(REFERENCE_ID_BUNDLE_EXTRA_NAME, reference?.id)

            outState.putString(REFERENCE_TITLE_BUNDLE_EXTRA_NAME, edtxtTitle.text.toString())
            outState.putString(REFERENCE_SERIES_BUNDLE_EXTRA_NAME, edtxtSeries.text.toString())
            outState.putString(REFERENCE_ISSUE_BUNDLE_EXTRA_NAME, edtxtIssue.text.toString())
            outState.putString(REFERENCE_PUBLISHING_DATE_BUNDLE_EXTRA_NAME, edtxtPublishingDate.text.toString())
            outState.putString(REFERENCE_URL_BUNDLE_EXTRA_NAME, edtxtUrl.text.toString())
        }
    }

    private fun setupUI() {
        setContentView(R.layout.activity_edit_reference)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnCreateNewReference.setOnClickListener { createReference() } // TODO: check if previous reference contains unsaved changes

        rcyExistingReferencesSearchResults.addItemDecoration(HorizontalDividerItemDecoration(this))
        rcyExistingReferencesSearchResults.adapter = existingReferencesSearchResultsAdapter
        existingReferencesSearchResultsAdapter.itemClickListener = { item -> existingReferenceSelected(item) }

        edtxtFindReferences.addTextChangedListener(edtxtFindReferencesTextWatcher)

        btnSelectPublishingDate.setOnClickListener { showDatePickerDialog() }

        edtxtPublishingDate.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus == false) {
                edtxtPublishingDateLostFocus(edtxtPublishingDate.text.toString())
            }
        }
    }


    override fun onDestroy() {
        unregisterEventBusListener()

        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_reference_menu, menu)

        actionItemHelper.setupLayout(menu) { menuItem -> onOptionsItemSelected(menuItem) }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                closeDialog()
                return true
            }
            R.id.mnSaveReference -> {
                saveReferenceAndCloseDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun saveReferenceAndCloseDialog() {
        unregisterEventBusListener()

        if(edtxtPublishingDate.hasFocus()) { // OnFocusChangeListener doesn't get called when save action gets pressed
            edtxtPublishingDateLostFocus(edtxtPublishingDate.text.toString())
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
        reference?.let { reference ->
            reference.title = edtxtTitle.text.toString()
//            reference.series = edtxtSeries.text.toString()
            reference.issue = edtxtIssue.text.toString()
            reference.publishingDate = currentlySetPublishingDate
            reference.url = edtxtUrl.text.toString()

            presenter.saveReferenceAsync(reference) { successful ->
                if(successful) {
                    setActivityResult(EditReferenceActivityResult(didSaveReference = true, savedReference = reference))
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
        val parsedDate = presenter.parsePublishingDate(enteredText)

        if(parsedDate != null) {
            showPublishingDate(parsedDate)
        }
        else {
            edtxtPublishingDate.error = getString(R.string.activity_edit_reference_error_could_not_parse_publishing_date, enteredText)
        }
    }

    private fun showDatePickerDialog() {
        reference?.let { reference ->
            val pickDateDialog = PickDateDialog()

            pickDateDialog.show(supportFragmentManager, reference.publishingDate) { selectedDate ->
                showPublishingDate(selectedDate)
            }
        }
    }


    private fun showParameters(parameters: EditReferenceActivityParameters?) {
        parameters?.let {
            if(parameters.reference != null) {
                showReference(parameters.reference)
            }
            else {
                createReference()
            }

            if(parameters.ofEntry != null) {
                lytSetEntryReferenceControls.visibility = View.VISIBLE
            }
        }
    }

    private fun createReference() {
        showReference(Reference(""))
    }

    private fun showReference(referenceId: String) {
        referenceService.retrieve(referenceId)?.let { reference ->
            showReference(reference)
        }
    }

    private fun showReference(reference: Reference) {
        this.reference = reference
        existingReferencesSearchResultsAdapter.selectedReference = reference

        edtxtTitle.setText(reference.title)
        reference.series?.let { edtxtSeries.setText(it.title) }
        edtxtIssue.setText(reference.issue)

        showPublishingDate(reference.publishingDate)

        edtxtUrl.setText(reference.url)

        mayRegisterEventBusListener()
    }

    private fun showPublishingDate(publishingDate: Date?) {
        this.currentlySetPublishingDate = publishingDate

        publishingDate?.let { edtxtPublishingDate.setText(presenter.convertPublishingDateToText(it)) }
        if(publishingDate == null) {
            edtxtPublishingDate.setText("")
        }
    }


    private fun mayRegisterEventBusListener() {
        if(reference?.isPersisted() ?: false) {
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

    private fun referenceHasBeenEdited(reference: Reference) {
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

    private fun retrievedExistingReferencesSearchResultsOnUiThread(searchResults: List<Reference>) {
        existingReferencesSearchResultsAdapter.items = searchResults

        showRecyclerViewExistingReferencesSearchResults()
    }

    private fun existingReferenceSelected(reference: Reference) {
        edtxtFindReferences.hideKeyboard()

        hideRecyclerViewExistingReferencesSearchResults()

        // TODO: check if previous reference has unsaved changes
        showReference(reference)
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
            if(change.entity == reference) {
                referenceHasBeenEdited(change.entity)
            }
        }
    }

}
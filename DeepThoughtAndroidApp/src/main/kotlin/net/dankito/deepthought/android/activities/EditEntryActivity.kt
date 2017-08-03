package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_edit_entry.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityResult
import net.dankito.deepthought.android.activities.arguments.EditReferenceActivityResult
import net.dankito.deepthought.android.activities.arguments.EntryActivityParameters
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.TagsOnEntryDialogFragment
import net.dankito.deepthought.android.views.EntryFieldsPreview
import net.dankito.deepthought.android.views.html.AndroidHtmlEditor
import net.dankito.deepthought.android.views.html.AndroidHtmlEditorPool
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.data.TagService
import net.dankito.service.data.messages.EntryChanged
import net.dankito.service.eventbus.IEventBus
import net.dankito.utils.IThreadPool
import net.dankito.utils.serialization.ISerializer
import net.dankito.utils.ui.IDialogService
import net.engio.mbassy.listener.Handler
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.schedule


class EditEntryActivity : BaseActivity() {

    companion object {
        private const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        private const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        private const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"

        private const val CONTENT_INTENT_EXTRA_NAME = "CONTENT"
        private const val ABSTRACT_INTENT_EXTRA_NAME = "ABSTRACT"
        private const val TAGS_ON_ENTRY_INTENT_EXTRA_NAME = "TAGS_ON_ENTRY"

        const val ResultId = "EDIT_ENTRY_ACTIVITY_RESULT"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var threadPool: IThreadPool

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var htmlEditorPool: AndroidHtmlEditorPool


    private var entry: Entry? = null

    private var readLaterArticle: ReadLaterArticle? = null

    private var entryExtractionResult: EntryExtractionResult? = null


    private val tagsOnEntry: MutableList<Tag> = ArrayList()

    private var canEntryBeSaved = false

    private var entryHasBeenEdited = false


    private val presenter: EditEntryPresenter

    private lateinit var entryFieldsPreview: EntryFieldsPreview

    private lateinit var contentHtmlEditor: AndroidHtmlEditor

    private var mnSaveEntry: MenuItem? = null


    private var eventBusListener: EventBusListener? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditEntryPresenter(entryPersister, router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parameterHolder.setActivityResult(ResultId, EditEntryActivityResult())

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        showParameters(getParameters() as? EntryActivityParameters)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { editEntryExtractionResult(it) }
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> editEntry(entryId) }

        savedInstanceState.getString(CONTENT_INTENT_EXTRA_NAME)?.let { content ->
            Timer().schedule(100L) { contentHtmlEditor.setHtml(content) } // set delayed otherwise setHtml() from editEntry() wins
        }
        // TODO: restore abstract
        savedInstanceState.getString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME)?.let { tagsOnEntryIds -> restoreTagsOnEntryAsync(tagsOnEntryIds) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.let { outState ->
            outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, null)
            entry?.id?.let { entryId -> outState.putString(ENTRY_ID_INTENT_EXTRA_NAME, entryId) }

            outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, null)
            readLaterArticle?.id?.let { readLaterArticleId -> outState.putString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME, readLaterArticleId) }

            outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, null)
            entryExtractionResult?.let { outState.putString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME, serializer.serializeObject(it)) }

            outState.putString(TAGS_ON_ENTRY_INTENT_EXTRA_NAME, serializer.serializeObject(tagsOnEntry))

            outState.putString(CONTENT_INTENT_EXTRA_NAME, null)

            val countDownLatch = CountDownLatch(1)
            contentHtmlEditor.getHtmlAsync { content ->
                outState.putString(CONTENT_INTENT_EXTRA_NAME, content)
                countDownLatch.countDown()
            }
            try { countDownLatch.await(1, TimeUnit.SECONDS) } catch(ignored: Exception) { }
        }
    }


    private fun setupUI() {
        setContentView(R.layout.activity_edit_entry)

        setSupportActionBar(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)

            actionBar.title = ""
        }

        this.entryFieldsPreview = lytEntryFieldsPreview
        entryFieldsPreview.fieldClickedListener = { field -> entryFieldClicked(field)}

        setupEntryContentView()
    }

    private fun setupEntryContentView() {
        contentHtmlEditor = htmlEditorPool.getHtmlEditor(this, contentListener)

        lytEntryContent.addView(contentHtmlEditor, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val contentEditorParams = contentHtmlEditor.layoutParams as RelativeLayout.LayoutParams
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        contentHtmlEditor.layoutParams = contentEditorParams
    }


    override fun onResume() {
        super.onResume()

        (getAndClearResult(EditReferenceActivity.ResultId) as? EditReferenceActivityResult)?.let { result ->
            if(result.didSaveReference) {
                result.savedReference?.let { savedReference(it) }
            }
        }
    }

    private fun savedReference(reference: Reference) {
        entryFieldsPreview.reference = reference // do not set reference directly on entry as if entry is not saved get adding it to reference.entries causes an error

        updateCanEntryBeSavedOnUIThread(true)
        setReferencePreviewOnUIThread()
    }


    private fun entryFieldClicked(field: EntryField) {
        when(field) {
            EntryField.Abstract -> editAbstract()
            EntryField.Reference -> editReference()
            EntryField.Tags -> editTagsOnEntry()
        }
    }

    private fun editAbstract() {
        // TODO
    }

    private fun editReference() {
        setWaitingForResult(EditReferenceActivity.ResultId)

        val reference = entry?.reference ?: readLaterArticle?.entryExtractionResult?.reference ?: entryExtractionResult?.reference

        if(reference != null) {
            presenter.editReference(reference)
        }
        else {
            presenter.createReference()
        }
    }

    private fun editTagsOnEntry() {
        val tagsOnEntryDialog = TagsOnEntryDialogFragment()

        tagsOnEntryDialog.show(supportFragmentManager, tagsOnEntry) {
            tagsOnEntry.clear()
            tagsOnEntry.addAll(it)

            entryHasBeenEdited()

            runOnUiThread {
                updateCanEntryBeSavedOnUIThread(true)
                setTagsOnEntryPreviewOnUIThread()
            }
        }
    }

    private fun updateCanEntryBeSavedOnUIThread(canEntryBeSaved: Boolean) {
        this.canEntryBeSaved = canEntryBeSaved

        setMenuSaveEntryEnabledStateOnUIThread()
    }

    private fun setMenuSaveEntryEnabledStateOnUIThread() {
        mnSaveEntry?.isEnabled = canEntryBeSaved
    }


    private fun setAbstractPreviewOnUIThread() {
        entryFieldsPreview.setAbstractPreviewOnUIThread()
    }

    private fun setReferencePreviewOnUIThread() {
        entryFieldsPreview.setReferencePreviewOnUIThread()
    }

    private fun setTagsOnEntryPreviewOnUIThread() {
        entryFieldsPreview.setTagsOnEntryPreviewOnUIThread()
    }


    override fun onDestroy() {
        htmlEditorPool.htmlEditorReleased(contentHtmlEditor)

        parameterHolder.clearActivityResults(EditReferenceActivity.ResultId)

        unregisterEventBusListener()

        super.onDestroy()
    }

    override fun onBackPressed() {
        askIfUnsavedChangesShouldBeSavedAndCloseDialog()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_entry_menu, menu)

        mnSaveEntry = menu.findItem(R.id.mnSaveEntry)

        setMenuSaveEntryEnabledStateOnUIThread()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                askIfUnsavedChangesShouldBeSavedAndCloseDialog()
                return true
            }
            R.id.mnSaveEntry -> {
                saveEntryAndCloseDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun saveEntryAndCloseDialog() {
        mnSaveEntry?.isEnabled = false // disable to that save cannot be pressed a second time
        unregisterEventBusListener()

        saveEntryAsync { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
            else {
                mnSaveEntry?.isEnabled = true
                mayRegisterEventBusListener()
            }
        }
    }

    private fun saveEntryAsync(callback: (Boolean) -> Unit) {
        contentHtmlEditor.getHtmlAsync { content ->

            entry?.let { entry ->
                updateEntry(entry, content)
                presenter.saveEntryAsync(entry, entryFieldsPreview.reference, tagsOnEntry) { successful ->
                    if(successful) {
                        setActivityResult(EditEntryActivityResult(didSaveEntry = true, savedEntry = entry))
                    }
                    callback(successful)
                }
            }

            entryExtractionResult?.let { extractionResult ->
                updateEntry(extractionResult.entry, content)
                presenter.saveEntryAsync(extractionResult.entry, entryFieldsPreview.reference, tagsOnEntry) { successful ->
                    if(successful) {
                        setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedEntry = extractionResult.entry))
                    }
                    callback(successful)
                }
            }

            readLaterArticle?.let { readLaterArticle ->
                val extractionResult = readLaterArticle.entryExtractionResult
                updateEntry(extractionResult.entry, content)

                presenter.saveEntryAsync(extractionResult.entry, entryFieldsPreview.reference, tagsOnEntry) { successful ->
                    if(successful) {
                        readLaterArticleService.delete(readLaterArticle)
                        setActivityResult(EditEntryActivityResult(didSaveReadLaterArticle = true, savedEntry = extractionResult.entry))
                    }
                    callback(successful)
                }
            }
        }
    }

    private fun setActivityResult(result: EditEntryActivityResult) {
        parameterHolder.setActivityResult(ResultId, result)
    }

    private fun updateEntry(entry: Entry, content: String) {
        entry.content = content
    }


    private fun askIfUnsavedChangesShouldBeSavedAndCloseDialog() {
        if(entryHasBeenEdited) {
            askIfUnsavedChangesShouldBeSaved()
        }
        else {
            closeDialog()
        }
    }

    private fun askIfUnsavedChangesShouldBeSaved() {
        dialogService.showConfirmationDialog(getString(R.string.activity_edit_entry_alert_message_entry_contains_unsaved_changes)) { shouldChangedGetSaved ->
            runOnUiThread {
                if(shouldChangedGetSaved) {
                    saveEntryAndCloseDialog()
                }
                else {
                    closeDialog()
                }
            }
        }
    }

    private fun closeDialog() {
        finish()
    }


    private fun showParameters(parameters: EntryActivityParameters?) {
        if(parameters == null) { // create entry
            if(entry == null) { // entry != null -> entry has been restored from savedInstanceState, parameters therefor is null
                createEntry()
            }
        }
        else {
            parameters.entry?.let { editEntry(it) }

            parameters.readLaterArticle?.let { editReadLaterArticle(it) }

            parameters.entryExtractionResult?.let { editEntryExtractionResult(it) }

            parameters.field?.let { entryFieldClicked(it) }
        }
    }

    private fun createEntry() {
        canEntryBeSaved = true

        editEntry(Entry(""))
    }

    private fun editEntry(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            editEntry(entry)
        }
    }

    private fun editEntry(entry: Entry) {
        this.entry = entry
        entryFieldsPreview.entry = entry

        editEntry(entry, entry.reference, entry.tags)
    }

    private fun editReadLaterArticle(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            editReadLaterArticle(readLaterArticle)
        }
    }

    private fun editReadLaterArticle(readLaterArticle: ReadLaterArticle) {
        this.readLaterArticle = readLaterArticle
        entryFieldsPreview.readLaterArticle = readLaterArticle
        canEntryBeSaved = true

        editEntry(readLaterArticle.entryExtractionResult.entry, readLaterArticle.entryExtractionResult.reference, readLaterArticle.entryExtractionResult.tags)
    }

    private fun editEntryExtractionResult(serializedExtractionResult: String) {
        val extractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)

        editEntryExtractionResult(extractionResult)
    }

    private fun editEntryExtractionResult(extractionResult: EntryExtractionResult) {
        this.entryExtractionResult = extractionResult
        entryFieldsPreview.entryExtractionResult = this.entryExtractionResult
        canEntryBeSaved = true

        editEntry(entryExtractionResult?.entry, entryExtractionResult?.reference, entryExtractionResult?.tags)
    }

    private fun editEntry(entry: Entry?, reference: Reference?, tags: Collection<Tag>?) {
        entry?.let { contentHtmlEditor.setHtml(entry.content) }

        entryFieldsPreview.reference = reference

        setAbstractPreviewOnUIThread()

        setReferencePreviewOnUIThread()

        tags?.let {
            tagsOnEntry.addAll(tags)
            entryFieldsPreview.tagsOnEntry = tagsOnEntry

            setTagsOnEntryPreviewOnUIThread()
        }

        mayRegisterEventBusListener()
    }


    private fun restoreTagsOnEntryAsync(tagsOnEntryIdsString: String) {
        threadPool.runAsync { restoreTagsOnEntry(tagsOnEntryIdsString) }
    }

    private fun restoreTagsOnEntry(tagsOnEntryIdsString: String) {
        val restoredTagsOnEntry = serializer.deserializeObject(tagsOnEntryIdsString, List::class.java, Tag::class.java) as List<Tag>

        tagsOnEntry.clear()
        tagsOnEntry.addAll(restoredTagsOnEntry)

        runOnUiThread { setTagsOnEntryPreviewOnUIThread() }
    }


    private fun contentHasBeenEdited() {
        entryHasBeenEdited()
        runOnUiThread { updateCanEntryBeSavedOnUIThread(true) }
    }

    private fun entryHasBeenEdited() {
        entryHasBeenEdited = true
    }


    private val contentListener = object : IHtmlEditorListener {

        override fun editorHasLoaded(editor: HtmlEditorCommon) {
        }

        override fun htmlCodeUpdated() {
            contentHasBeenEdited()
        }

        override fun htmlCodeHasBeenReset() {
        }

    }


    private fun mayRegisterEventBusListener() {
        if(entry?.isPersisted() ?: false) {
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

    private fun entryHasBeenEdited(entry: Entry) {
        unregisterEventBusListener() // message now gets shown, don't display it a second time

        runOnUiThread {
            dialogService.showInfoMessage(getString(R.string.activity_edit_entry_alert_message_entry_has_been_edited))
        }
    }

    inner class EventBusListener {

        @Handler
        fun entryChanged(change: EntryChanged) {
            if(change.entity == entry) {
                entryHasBeenEdited(change.entity)
            }
        }
    }

}

package net.dankito.deepthought.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_edit_entry.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.activities.arguments.EditEntryActivityResult
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.dialogs.TagsOnEntryDialogFragment
import net.dankito.deepthought.android.views.EntryField
import net.dankito.deepthought.android.views.EntryFieldsPreview
import net.dankito.deepthought.android.views.html.AndroidHtmlEditor
import net.dankito.deepthought.android.views.html.AndroidHtmlEditorPool
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.html.HtmlEditorCommon
import net.dankito.deepthought.ui.html.IHtmlEditorListener
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.serialization.ISerializer
import javax.inject.Inject


class EditEntryActivity : BaseActivity() {

    companion object {
        const val ENTRY_ID_INTENT_EXTRA_NAME = "ENTRY_ID"
        const val READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME = "READ_LATER_ARTICLE_ID"
        const val ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME = "ENTRY_EXTRACTION_RESULT"

        const val ResultId = "EDIT_ENTRY_ACTIVITY_RESULT"
    }


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var serializer: ISerializer

    @Inject
    protected lateinit var htmlEditorPool: AndroidHtmlEditorPool


    private var entry: Entry? = null

    private var readLaterArticle: ReadLaterArticle? = null

    private var entryExtractionResult: EntryExtractionResult? = null


    private val tagsOnEntry: MutableList<Tag> = ArrayList()

    private var canEntryBeSaved = false


    private val presenter: EditEntryPresenter

    private lateinit var entryFieldsPreview: EntryFieldsPreview

    private lateinit var contentHtmlEditor: AndroidHtmlEditor

    private var mnSaveEntry: MenuItem? = null


    init {
        AppComponent.component.inject(this)

        presenter = EditEntryPresenter(entryPersister, router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parameterHolder.setActivityResult(ResultId, EditEntryActivityResult())

        setupUI()

        savedInstanceState?.let { restoreState(it) }

        intent.getStringExtra(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { editEntryExtractionResult(it) }
        intent.getStringExtra(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        intent.getStringExtra(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> editEntry(entryId) }

        if(entry == null && readLaterArticle == null && entryExtractionResult == null) { // create Entry
            entry = Entry("")
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getString(ENTRY_EXTRACTION_RESULT_INTENT_EXTRA_NAME)?.let { editEntryExtractionResult(it) }
        savedInstanceState.getString(READ_LATER_ARTICLE_ID_INTENT_EXTRA_NAME)?.let { readLaterArticleId -> editReadLaterArticle(readLaterArticleId) }
        savedInstanceState.getString(ENTRY_ID_INTENT_EXTRA_NAME)?.let { entryId -> editEntry(entryId) }
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
        }
    }


    private fun setupUI() {
        setContentView(R.layout.activity_edit_entry)

        setSupportActionBar(toolbar)

        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
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
        // TODO
    }

    private fun editTagsOnEntry() {
        val tagsOnEntryDialog = TagsOnEntryDialogFragment()

        tagsOnEntryDialog.show(supportFragmentManager, tagsOnEntry) {
            tagsOnEntry.clear()
            tagsOnEntry.addAll(it)

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


    override fun onStop() {
        htmlEditorPool.htmlEditorReleased(contentHtmlEditor)

        super.onStop()
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
                closeDialog()
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
        saveEntryAsync { successful ->
            if(successful) {
                runOnUiThread { closeDialog() }
            }
        }
    }

    private fun saveEntryAsync(callback: (Boolean) -> Unit) {
        contentHtmlEditor.getHtmlAsync { content ->

            entry?.let { entry ->
                updateEntry(entry, content)
                presenter.saveEntryAsync(entry, entry.reference, tagsOnEntry) { successful ->
                    if(successful) {
                        setActivityResult(EditEntryActivityResult(didSaveEntry = true, savedEntry = entry))
                    }
                    callback(successful)
                }
            }

            entryExtractionResult?.let { extractionResult ->
                updateEntry(extractionResult.entry, content)
                presenter.saveEntryAsync(extractionResult.entry, extractionResult.reference, tagsOnEntry) { successful ->
                    if(successful) {
                        setActivityResult(EditEntryActivityResult(didSaveEntryExtractionResult = true, savedEntry = entry))
                    }
                    callback(successful)
                }
            }

            readLaterArticle?.let { readLaterArticle ->
                val extractionResult = readLaterArticle.entryExtractionResult
                updateEntry(extractionResult.entry, content)

                presenter.saveEntryAsync(extractionResult.entry, extractionResult.reference, tagsOnEntry) { successful ->
                    if(successful) {
                        readLaterArticleService.delete(readLaterArticle)
                        setActivityResult(EditEntryActivityResult(didSaveReadLaterArticle = true, savedEntry = entry))
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

    private fun closeDialog() {
        finish()
    }


    private fun editEntry(entryId: String) {
        entryService.retrieve(entryId)?.let { entry ->
            this.entry = entry
            entryFieldsPreview.entry = entry

            editEntry(entry, entry.reference, entry.tags)
        }
    }

    private fun editReadLaterArticle(readLaterArticleId: String) {
        readLaterArticleService.retrieve(readLaterArticleId)?.let { readLaterArticle ->
            this.readLaterArticle = readLaterArticle
            entryFieldsPreview.readLaterArticle = readLaterArticle
            canEntryBeSaved = true

            editEntry(readLaterArticle.entryExtractionResult.entry, readLaterArticle.entryExtractionResult.reference, readLaterArticle.entryExtractionResult.tags)
        }
    }

    private fun editEntryExtractionResult(serializedExtractionResult: String) {
        this.entryExtractionResult = serializer.deserializeObject(serializedExtractionResult, EntryExtractionResult::class.java)
        entryFieldsPreview.entryExtractionResult = this.entryExtractionResult
        canEntryBeSaved = true

        editEntry(entryExtractionResult?.entry, entryExtractionResult?.reference, entryExtractionResult?.tags)
    }

    private fun editEntry(entry: Entry?, reference: Reference?, tags: Collection<Tag>?) {
        entry?.let { contentHtmlEditor.setHtml(entry.content) }

        setAbstractPreviewOnUIThread()

        setReferencePreviewOnUIThread()

        tags?.let {
            tagsOnEntry.addAll(tags)
            entryFieldsPreview.tagsOnEntry = tagsOnEntry

            setTagsOnEntryPreviewOnUIThread()
        }
    }


    private val contentListener = object : IHtmlEditorListener {

        override fun editorHasLoaded(editor: HtmlEditorCommon) {
        }

        override fun htmlCodeUpdated() {
            runOnUiThread { updateCanEntryBeSavedOnUIThread(true) }
        }

        override fun htmlCodeHasBeenReset() {
        }

    }

}

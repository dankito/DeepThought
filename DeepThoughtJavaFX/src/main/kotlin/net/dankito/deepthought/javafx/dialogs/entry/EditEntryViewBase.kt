package net.dankito.deepthought.javafx.dialogs.entry

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Control
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.entry.controls.EditItemSourceField
import net.dankito.deepthought.javafx.dialogs.entry.controls.EditItemTagsField
import net.dankito.deepthought.javafx.dialogs.entry.controls.InlineHtmlEditor
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.abstractPlainText
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


abstract class EditEntryViewBase : DialogFragment() {

    // param values for Item and ItemExtractionResult are evaluated after root has been initialized -> Item is null at root initialization stage.
    // so i had to find a way to mitigate that Item / ItemExtractionResult is not initialized yet

    protected val editedSummary = SimpleStringProperty()

    protected val contentHtml = SimpleStringProperty()

    protected val hasUnsavedChanges = SimpleBooleanProperty()

    protected var canAlwaysBeSaved = false // ReadLaterArticles and ItemExtractionResults can always be saved, no matter if they contain changes or not


    private val editSourceField = EditItemSourceField()

    private var editTagsField: EditItemTagsField by singleAssign()

    private val htmlEditor = InlineHtmlEditor()

    private var wbvwShowUrl: WebView by singleAssign()


    private val presenter: EditEntryPresenter


    private var item: Item? = null

    private var originalSummary = ""

    private var currentlyDisplayedUrl: String? = null


    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)

        presenter = EditEntryPresenter(entryPersister, readLaterArticleService, clipboardService, router)
    }


    override var root = vbox {
        prefWidth = 905.0
        prefHeight = 650.0

        add(editSourceField.root)
        editSourceField.didEntityChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
        editSourceField.didTitleChange.addListener { _, _, _ -> updateHasUnsavedChanges() }

        editedSummary.addListener { _, _, _ -> updateHasUnsavedChanges() }

        hbox {
            alignment = Pos.TOP_LEFT
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.item.summary.label"]) {
                minWidth = Control.USE_PREF_SIZE // guarantee that label keeps its calculated size
            }

            textarea {
                isWrapText = true
                prefHeight = 20.0
                maxHeight = 100.0

                textProperty().bindBidirectional(editedSummary)

                runLater { requestFocus() /* so that Source title text field isn't focused and source search results therefore shown */ }

                hboxConstraints {
                    hGrow = Priority.ALWAYS

                    marginLeft = 4.0
                }
            }

            vboxConstraints {
                marginTop = 6.0
                marginBottom = 6.0
            }
        }

        editTagsField = EditItemTagsField()
        editTagsField.didCollectionChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
        add(editTagsField.root)

        VBox.setMargin(editTagsField.root, Insets(0.0, 0.0, 6.0, 0.0))


        add(htmlEditor)

        htmlEditor.minHeight = 50.0
        contentHtml.onChange { htmlEditor.setHtml(contentHtml.value) }
        htmlEditor.javaScriptExecutor.addDidHtmlChangeListener { updateHasUnsavedChanges() }


        wbvwShowUrl = webview {
            useMaxWidth = true
            isVisible = false
            FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

            vboxConstraints {
                vgrow = Priority.ALWAYS
            }
        }

        wbvwShowUrl.engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if(newState === Worker.State.SUCCEEDED) {
                val html = wbvwShowUrl.engine.executeScript("document.documentElement.outerHTML") as String
                currentlyDisplayedUrl?.let { urlLoaded(it, html) }
            }
        }

        val buttons = DialogButtonBar({ closeDialog() }, { saveEntryAsync(it) }, hasUnsavedChanges, messages["action.save"])
        add(buttons)
    }


    protected open fun urlLoaded(url: String, html: String) {

    }


    override fun onUndock() {
        cleanUp()

        super.onUndock()
    }

    private fun cleanUp() {
        contentHtml.onChange { }

        htmlEditor.cleanUp()

        System.gc()
    }


    protected fun showData(item: Item, tags: MutableCollection<Tag>, source: Source?, series: Series?, contentToEdit: String? = null) {
        this.item = item
        originalSummary = item.abstractPlainText

        editedSummary.value = originalSummary

        showContent(item, source, contentToEdit)

        editSourceField.setSourceToEdit(source, series)
        editTagsField.setCollectionToEdit(tags)
    }

    private fun showContent(item: Item, source: Source?, contentToEdit: String?) {
        val content = contentToEdit ?: item.content

        if(content.isNullOrBlank() && source?.url != null) { // content could not get extracted yet -> show url and when its html is loaded try to extract content then
            source.url?.let { url ->
                wbvwShowUrl.engine.load(url)
                currentlyDisplayedUrl = url

                htmlEditor.isVisible = false
                wbvwShowUrl.isVisible = true
                wbvwShowUrl.requestFocus() // so that Source title text field isn't focused and source search results therefore shown
            }
        }
        else {
            contentHtml.value = content

            htmlEditor.isVisible = true
            wbvwShowUrl.isVisible = false
            htmlEditor.focusEditor() // so that Source title text field isn't focused and source search results therefore shown
        }
    }


    private fun updateHasUnsavedChanges() {
        hasUnsavedChanges.value = canAlwaysBeSaved || htmlEditor.didHtmlChange || editSourceField.didEntityChange.value || editSourceField.didTitleChange.value
                || editedSummary.value != originalSummary || editTagsField.didCollectionChange.value
    }


    private fun saveEntryAsync(done: () -> Unit) {
        updateEntryAndSaveAsync {
            entrySaved()

            done()
        }
    }

    private fun updateEntryAndSaveAsync(done: () -> Unit) {
        item?.let { entry ->
            entry.content = htmlEditor.getHtml()
            entry.summary = editedSummary.value

            val source = updateSource()

            presenter.saveEntryAsync(entry, source, editSourceField.seriesToEdit, editTagsField.applyChangesAndGetTags()) {
                done()
            }
        }
    }

    private fun updateSource(): Source? {
        var source = editSourceField.sourceToEdit

        if(editSourceField.didTitleChange.value) {
            source?.title = editSourceField.enteredTitle
        }

        if(source?.isPersisted() == false && editSourceField.enteredTitle.isNullOrBlank()) {
            source = null
            resetSeries()
        }

        if(source != editSourceField.originalSource) {
            resetSeries()
        }

        return source
    }

    protected open fun resetSeries() {

    }

    protected open fun entrySaved() {

    }

    private fun closeDialog() {
        runLater {
            cleanUp()

            close()
        }
    }

}
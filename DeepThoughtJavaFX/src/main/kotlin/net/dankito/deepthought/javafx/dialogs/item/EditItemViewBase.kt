package net.dankito.deepthought.javafx.dialogs.item

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Control
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import net.dankito.deepthought.data.ItemPersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.item.controls.EditItemSourceField
import net.dankito.deepthought.javafx.dialogs.item.controls.EditItemTagsField
import net.dankito.deepthought.javafx.dialogs.item.controls.InlineHtmlEditor
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.ui.controls.EditEntityFilesField
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditItemPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


abstract class EditItemViewBase : DialogFragment() {

    // param values for Item and ItemExtractionResult are evaluated after root has been initialized -> Item is null at root initialization stage.
    // so i had to find a way to mitigate that Item / ItemExtractionResult is not initialized yet

    protected val editedSummary = SimpleStringProperty()

    protected val contentHtml = SimpleStringProperty()

    protected val hasUnsavedChanges = SimpleBooleanProperty()

    protected var canAlwaysBeSaved = false // ReadLaterArticles and ItemExtractionResults can always be saved, no matter if they contain changes or not


    private val editSourceField = EditItemSourceField()

    private var editTagsField: EditItemTagsField by singleAssign()

    private var editFilesField: EditEntityFilesField by singleAssign()

    private val htmlEditor = InlineHtmlEditor()

    private var wbvwShowUrl: WebView by singleAssign()


    private val presenter: EditItemPresenter


    private var item: Item? = null

    private var originalSummary = ""

    private var currentlyDisplayedUrl: String? = null


    @Inject
    protected lateinit var itemPersister: ItemPersister

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

        presenter = EditItemPresenter(itemPersister, readLaterArticleService, clipboardService, router)
    }


    override var root = vbox {
        prefWidth = 905.0
        prefHeight = 650.0

        add(editSourceField.root)
        editSourceField.didEntityChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
        editSourceField.didTitleChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
        editSourceField.didIndicationChange.addListener { _, _, _ -> updateHasUnsavedChanges() }

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

        editFilesField = EditEntityFilesField()
        editFilesField.didValueChange.addListener { _, _, _ -> updateHasUnsavedChanges() }
        add(editFilesField.root)

        VBox.setMargin(editTagsField.root, Insets(0.0, 0.0, 6.0, 0.0))


        add(htmlEditor)

        htmlEditor.minHeight = 50.0
        contentHtml.onChange { htmlEditor.setHtml(contentHtml.value, editSourceField.sourceToEdit?.url) }
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
                currentlyDisplayedUrl?.let {
                    val html = wbvwShowUrl.engine.executeScript("document.documentElement.outerHTML") as String
                    urlLoaded(it, html)
                }
            }
        }

        val buttons = DialogButtonBar({ closeDialog() }, { saveItemAsync(it) }, hasUnsavedChanges, messages["action.save"])
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

        (wbvwShowUrl.parent as? Pane)?.children?.remove(wbvwShowUrl)

        System.gc() // WebView is known for consuming a lot of memory (and causing memory leaks), may this helps some
    }


    protected fun showData(item: Item, tags: MutableCollection<Tag>, source: Source?, series: Series?, files: MutableCollection<FileLink>, contentToEdit: String? = null) {
        this.item = item
        originalSummary = item.summaryPlainText

        editedSummary.value = originalSummary

        editSourceField.setSourceToEdit(source, series, item.indication)
        editTagsField.setCollectionToEdit(tags)
        editFilesField.setFiles(files)

        showContent(item, source, contentToEdit) // set after editSourceField.setSourceToEdit() so that sourceToEdit is set when contentHtml.onChange { } is called
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
        hasUnsavedChanges.value = canAlwaysBeSaved || htmlEditor.didHtmlChange
                || editSourceField.didEntityChange.value || editSourceField.didTitleChange.value || editSourceField.didIndicationChange.value
                || editedSummary.value != originalSummary || editTagsField.didCollectionChange.value || editFilesField.didValueChange.value
    }


    private fun saveItemAsync(done: () -> Unit) {
        updateItemAndSaveAsync {
            itemSaved()

            done()
        }
    }

    private fun updateItemAndSaveAsync(done: () -> Unit) {
        item?.let { item ->
            item.content = htmlEditor.getHtml()
            item.summary = editedSummary.value
            item.indication = editSourceField.enteredIndication

            val source = updateSource()

            presenter.saveItemAsync(item, source, editSourceField.seriesToEdit, editTagsField.applyChangesAndGetTags(), editFilesField.getEditedFiles()) {
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
            resetSeries() // TODO: is this really necessary as we then pass editSourceField.seriesToEdit to ItemPersister -> does editSourceField.seriesToEdit know now that series changed?
        }

        if(source != editSourceField.originalSource) {
            resetSeries() // TODO: is this really necessary as we then pass editSourceField.seriesToEdit to ItemPersister -> does editSourceField.seriesToEdit know now that series changed?
        }

        return source
    }

    protected open fun resetSeries() {

    }

    protected open fun itemSaved() {

    }

    private fun closeDialog() {
        runLater {
            cleanUp()

            close()
        }
    }

}
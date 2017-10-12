package net.dankito.deepthought.javafx.dialogs.entry

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.concurrent.Worker
import javafx.scene.Cursor
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import javafx.stage.StageStyle
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.ui.controls.JavaFXHtmlEditor
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.deepthought.data.EntryPersister
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.utils.ui.IClipboardService
import org.jsoup.Jsoup
import tornadofx.*
import javax.inject.Inject


abstract class EditEntryViewBase : DialogFragment() {

    // param values for Entry and EntryExtractionResult are evaluated after root has been initialized -> Entry is null at root initialization stage.
    // so i had to find a way to mitigate that Entry / EntryExtractionResult is not initialized yet

    protected val abstractPlainText = SimpleStringProperty()

    protected val referencePreview = SimpleStringProperty()

    protected val tagsPreview = SimpleStringProperty()

    protected val contentHtml = SimpleStringProperty()

    protected val hasUnsavedChanges = SimpleBooleanProperty()


    private var txtAbstract: Label by singleAssign()

    private var txtReference: Label by singleAssign()

    private var txtTags: Label by singleAssign()

    private val htmlEditor: JavaFXHtmlEditor

    private var wbvwShowUrl: WebView by singleAssign()

    private var tagsOnEntryDialog: TagsOnEntryDialog? = null

    private var editAbstractDialog: EditHtmlDialog? = null


    private val presenter: EditEntryPresenter


    private var entry: Entry? = null

    private var abstractToEdit = ""

    private val tagsOnEntry: ObservableSet<Tag> = FXCollections.observableSet()

    private var referenceToEdit: Reference? = null

    private var seriesToEdit: Series? = null

    private var currentlyDisplayedUrl: String? = null


    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var router: IRouter


    init {
        AppComponent.component.inject(this)

        presenter = EditEntryPresenter(entryPersister, readLaterArticleService, clipboardService, router)

        htmlEditor = JavaFXHtmlEditor(null)

        tagsOnEntry.addListener(SetChangeListener<Tag> { showTagsPreview(tagsOnEntry) } )
    }


    override var root = vbox {
        prefWidth = 905.0
        prefHeight = 650.0

        hbox {
            prefHeight = 20.0
            maxHeight = 100.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            cursor = Cursor.HAND
            setOnMouseClicked { abstractPreviewClicked(it) }

            label(messages["edit.entry.abstract.label"]) {
                minWidth = Control.USE_PREF_SIZE // guarantee that label keeps its calculated size
                useMaxWidth = true
            }

            txtAbstract = label {
                isWrapText = true

                textProperty().bind(abstractPlainText)
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        hbox {
            prefHeight = 20.0
            maxHeight = 70.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.entry.reference.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true
            }

            txtReference = label {
                isWrapText = false

                textProperty().bind(referencePreview)

                hgrow = Priority.ALWAYS
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        hbox {
            prefHeight = 20.0
            maxHeight = 70.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            cursor = Cursor.HAND
            setOnMouseClicked { tagsPreviewClicked(it) }

            label(messages["edit.entry.tags.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true
            }

            txtTags = label {
                isWrapText = false

                textProperty().bind(tagsPreview)

                hgrow = Priority.ALWAYS
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        add(htmlEditor)
        htmlEditor.vgrow = Priority.ALWAYS
        htmlEditor.useMaxWidth = true
        htmlEditor.prefWidthProperty().bind(this@vbox.widthProperty())
        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(htmlEditor)
        contentHtml.onChange { htmlEditor.setHtml(contentHtml.value) }

        wbvwShowUrl = webview {
            isVisible = false
            useMaxWidth = true

            vboxConstraints {
                vgrow = Priority.ALWAYS
            }
        }
        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(wbvwShowUrl)

        wbvwShowUrl.engine.loadWorker.stateProperty().addListener { _, _, newState ->
            if(newState === Worker.State.SUCCEEDED) {
                val html = wbvwShowUrl.engine.executeScript("document.documentElement.outerHTML") as String
                currentlyDisplayedUrl?.let { urlLoaded(it, html) }
            }
        }

        val buttons = DialogButtonBar({ closeDialog() }, { saveEntryAsync(it) }, hasUnsavedChanges.value, messages["action.save"])
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

        htmlEditor.prefWidthProperty().unbind()
        htmlEditor.cleanUp()

        root.children.remove(htmlEditor)

        System.gc()
    }


    private fun tagsPreviewClicked(event: MouseEvent) {
        if(event.button == MouseButton.PRIMARY) {
            if(tagsOnEntryDialog == null) {
                tagsOnEntryDialog = find(TagsOnEntryDialog::class, mapOf(TagsOnEntryDialog::tagsOnEntry to tagsOnEntry))
                tagsOnEntryDialog?.show(messages["tags.on.entry.dialog.title"], stageStyle = StageStyle.UTILITY, owner = currentStage) // TODO: add icon
            }
            else {
                tagsOnEntryDialog?.close()
                tagsOnEntryDialog = null
            }
        }
    }

    private fun abstractPreviewClicked(event: MouseEvent) {
        if(event.button == MouseButton.PRIMARY) {
            if(editAbstractDialog == null) {
                editAbstractDialog = find(EditHtmlDialog::class)
                editAbstractDialog?.show(abstractToEdit, messages["edit.entry.abstract.dialog.title"], currentStage, { this.editAbstractDialog = null } ) { // TODO: add icon
                    abstractToEdit = it
                    abstractPlainText.value = Jsoup.parseBodyFragment(abstractToEdit).text()
                }
            }
            else {
                editAbstractDialog?.currentStage?.requestFocus()
            }
        }
    }


    protected fun showData(entry: Entry, tags: Collection<Tag>, reference: Reference?, series: Series?, contentToEdit: String? = null) {
        this.entry = entry
        abstractToEdit = entry.abstractString
        tagsOnEntry.addAll(tags) // make a copy
        referenceToEdit = reference
        seriesToEdit = series

        abstractPlainText.value = Jsoup.parseBodyFragment(abstractToEdit).text()

        showContent(entry, reference, contentToEdit)

        showTagsPreview(tagsOnEntry)
        showReferencePreview(reference, series)
    }

    private fun showContent(entry: Entry, reference: Reference?, contentToEdit: String?) {
        val content = contentToEdit ?: entry.content

        if(content.isNullOrBlank() && reference?.url != null) { // content could not get extracted yet -> show url and when its html is loaded try to extract content then
            reference.url?.let { url ->
                wbvwShowUrl.engine.load(url)
                currentlyDisplayedUrl = url

                htmlEditor.isVisible = false
                wbvwShowUrl.isVisible = true
            }
        }
        else {
            contentHtml.value = content

            htmlEditor.isVisible = true
            wbvwShowUrl.isVisible = false
        }
    }

    private fun showReferencePreview(reference: Reference?, series: Series?) {
        this.referencePreview.value = reference?.getPreviewWithSeriesAndPublishingDate(series) ?: ""
    }

    private fun showTagsPreview(tags: Collection<Tag>) {
        this.tagsPreview.value = tags.sortedBy { it.name.toLowerCase() }.joinToString { it.name }
    }


    private fun saveEntryAsync(done: () -> Unit) {
        updateEntryAndSaveAsync {
            entrySaved()

            done()
        }
    }

    private fun updateEntryAndSaveAsync(done: () -> Unit) {
        htmlEditor.getHtmlAsync {
            entry?.let { entry ->
                entry.content = it
                entry.abstractString = abstractToEdit

                presenter.saveEntryAsync(entry, referenceToEdit, seriesToEdit, tagsOnEntry) {
                    done()
                }
            }
        }
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
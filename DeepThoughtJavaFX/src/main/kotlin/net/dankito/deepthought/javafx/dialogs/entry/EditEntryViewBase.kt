package net.dankito.deepthought.javafx.dialogs.entry

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.concurrent.Worker
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import javafx.stage.StageStyle
import net.dankito.deepthought.data.EntryPersister
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.DialogFragment
import net.dankito.deepthought.javafx.dialogs.entry.controls.SourceListCellFragment
import net.dankito.deepthought.javafx.dialogs.source.EditSourceDialog
import net.dankito.deepthought.javafx.ui.controls.DialogButtonBar
import net.dankito.deepthought.javafx.ui.controls.JavaFXHtmlEditor
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EditEntryPresenter
import net.dankito.service.data.ReadLaterArticleService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ReferenceSearch
import net.dankito.utils.extensions.toSortedString
import net.dankito.utils.ui.IClipboardService
import org.jsoup.Jsoup
import tornadofx.*
import javax.inject.Inject


abstract class EditEntryViewBase : DialogFragment() {

    // param values for Item and ItemExtractionResult are evaluated after root has been initialized -> Item is null at root initialization stage.
    // so i had to find a way to mitigate that Item / ItemExtractionResult is not initialized yet

    protected val isSourceSet = SimpleBooleanProperty()

    protected val sourcePreview = SimpleStringProperty()

    protected val showSourceSearchResult = SimpleBooleanProperty()

    protected val abstractPlainText = SimpleStringProperty()

    protected val tagsPreview = SimpleStringProperty()

    protected val contentHtml = SimpleStringProperty()

    protected val hasUnsavedChanges = SimpleBooleanProperty()


    private var txtAbstract: Label by singleAssign()

    private var txtfldSearchSource: TextField by singleAssign()

    private var txtSourcePreview: Label by singleAssign()

    private var lstSourceSearchResult: ListView<Source> by singleAssign()

    private var txtTags: Label by singleAssign()

    private val htmlEditor: JavaFXHtmlEditor

    private var wbvwShowUrl: WebView by singleAssign()

    private var tagsOnEntryDialog: TagsOnEntryDialog? = null

    private var editAbstractDialog: EditHtmlDialog? = null

    private var editSourceDialog: EditSourceDialog? = null


    private val presenter: EditEntryPresenter

    private val sourceSearchResults: ObservableList<Source> = FXCollections.observableArrayList()

    private var lastSourceSearch: ReferenceSearch? = null


    private var item: Item? = null

    private var abstractToEdit = ""

    private val tagsOnEntry: ObservableSet<Tag> = FXCollections.observableSet()

    private var sourceToEdit: Source? = null

    private var seriesToEdit: Series? = null

    private var currentlyDisplayedUrl: String? = null


    @Inject
    protected lateinit var entryPersister: EntryPersister

    @Inject
    protected lateinit var readLaterArticleService: ReadLaterArticleService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

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
            maxHeight = 70.0
            alignment = Pos.CENTER_LEFT
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.item.source.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true
            }

            txtSourcePreview = label {
                isWrapText = false

                textProperty().bind(sourcePreview)
                visibleProperty().bind(isSourceSet)
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                cursor = Cursor.HAND
                setOnMouseClicked { sourceClicked(it) }

                hgrow = Priority.ALWAYS
            }

            txtfldSearchSource = textfield {
                visibleProperty().bind(isSourceSet.not())
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                hgrow = Priority.ALWAYS

                promptText = messages["find.source.prompt.text"]

                textProperty().addListener { _, _, newValue -> searchSources(newValue) }
                setOnKeyReleased { event ->
                    if(event.code == KeyCode.ENTER) {
                        createOrSelectSource()
                    }
                    else if(event.code == KeyCode.ESCAPE) {
                        clear()
                        hideSourceSearchResult()
                    }
                }
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        lstSourceSearchResult = listview(sourceSearchResults) {
            vgrow = Priority.ALWAYS
            visibleProperty().bind(showSourceSearchResult)
            FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

            cellFragment(SourceListCellFragment::class)

            onDoubleClick { setSource(selectionModel.selectedItem) }
        }

        hbox {
            prefHeight = 20.0
            maxHeight = 100.0
            prefWidthProperty().bind(this@vbox.widthProperty())

            cursor = Cursor.HAND
            setOnMouseClicked { abstractPreviewClicked(it) }

            label(messages["edit.item.summary.label"]) {
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

            cursor = Cursor.HAND
            setOnMouseClicked { tagsPreviewClicked(it) }

            label(messages["edit.item.tags.label"]) {
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

        // TODO: we're not setting hasUnsavedChanges when there are changes so set hasUnsavedChanges to true in order to enable save button
        hasUnsavedChanges.value = true
        val buttons = DialogButtonBar({ closeDialog() }, { saveEntryAsync(it) }, hasUnsavedChanges, messages["action.save"])
        add(buttons)
    }

    private fun searchSources(searchTerm: String) {
        lastSourceSearch?.interrupt()

        lastSourceSearch = ReferenceSearch(searchTerm) { result ->
            FXUtils.runOnUiThread { retrievedSourceSearchResultsOnUiThread(result) }
        }

        lastSourceSearch?.let { searchEngine.searchReferences(it) }
    }

    private fun retrievedSourceSearchResultsOnUiThread(result: List<Source>) {
        sourceSearchResults.setAll(result)
        showSourceSearchResult.value = true
    }

    private fun createOrSelectSource() {
        if(sourceSearchResults.size == 1) {
            setSource(sourceSearchResults[0])
        }
        else if(sourceSearchResults.size == 0 && txtfldSearchSource.text.isNotBlank()) {
            hideSourceSearchResult()
            // TODO: create Source
        }
    }

    private fun setSource(source: Source?) {
        sourceToEdit = source

        hideSourceSearchResult()
        showSourcePreview(source, source?.series)
    }

    private fun hideSourceSearchResult() {
        showSourceSearchResult.value = false
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
                tagsOnEntryDialog?.show(messages["tags.on.item.dialog.title"], stageStyle = StageStyle.UTILITY, owner = currentStage) // TODO: add icon
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
                editAbstractDialog?.show(abstractToEdit, messages["edit.item.summary.dialog.title"], currentStage, { this.editAbstractDialog = null } ) { // TODO: add icon
                    abstractToEdit = it
                    abstractPlainText.value = Jsoup.parseBodyFragment(abstractToEdit).text()
                }
            }
            else {
                editAbstractDialog?.currentStage?.requestFocus()
            }
        }
    }

    private fun sourceClicked(event: MouseEvent) {
        if(event.button == MouseButton.PRIMARY) {
            if(editSourceDialog == null) {
                editSourceDialog = find(EditSourceDialog::class, mapOf(EditSourceDialog::source to (sourceToEdit ?: Source(""))))
                editSourceDialog?.show(messages["edit.item.summary.dialog.title"], owner = currentStage ) // TODO: add icon
            }
            else {
                editSourceDialog?.currentStage?.requestFocus()
            }
        }
    }


    protected fun showData(item: Item, tags: Collection<Tag>, source: Source?, series: Series?, contentToEdit: String? = null) {
        this.item = item
        abstractToEdit = item.summary
        tagsOnEntry.addAll(tags) // make a copy
        sourceToEdit = source
        seriesToEdit = series

        abstractPlainText.value = Jsoup.parseBodyFragment(abstractToEdit).text()

        showContent(item, source, contentToEdit)

        showTagsPreview(tagsOnEntry)
        showSourcePreview(source, series)
    }

    private fun showContent(item: Item, source: Source?, contentToEdit: String?) {
        val content = contentToEdit ?: item.content

        if(content.isNullOrBlank() && source?.url != null) { // content could not get extracted yet -> show url and when its html is loaded try to extract content then
            source.url?.let { url ->
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

    private fun showSourcePreview(source: Source?, series: Series?) {
        this.isSourceSet.value = source != null
        this.sourcePreview.value = source?.getPreviewWithSeriesAndPublishingDate(series) ?: ""
    }

    private fun showTagsPreview(tags: Collection<Tag>) {
        this.tagsPreview.value = tags.toSortedString()
    }


    private fun saveEntryAsync(done: () -> Unit) {
        updateEntryAndSaveAsync {
            entrySaved()

            done()
        }
    }

    private fun updateEntryAndSaveAsync(done: () -> Unit) {
        htmlEditor.getHtmlAsync {
            item?.let { entry ->
                entry.content = it
                entry.summary = abstractToEdit

                presenter.saveEntryAsync(entry, sourceToEdit, seriesToEdit, tagsOnEntry) {
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
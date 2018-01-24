package net.dankito.deepthought.javafx.dialogs.entry.controls

import com.sun.javafx.scene.traversal.Direction
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.res.icons.Icons
import net.dankito.deepthought.javafx.service.events.EditingSourceDoneEvent
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getSeriesAndPublishingDatePreview
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.richtexteditor.java.fx.extensions.setImageTintColor
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import net.engio.mbassy.listener.Handler
import tornadofx.*
import javax.inject.Inject


class EditItemSourceField : View() {

    var sourceToEdit: Source? = null
        private set

    var seriesToEdit: Series? = null
        private set

    val didEntityChange = SimpleBooleanProperty()

    val didTitleChange = SimpleBooleanProperty()

    val didIndicationChange = SimpleBooleanProperty()

    val enteredTitle: String
        get() = editedSourceTitle.value

    val enteredIndication: String
        get() = editedIndication.value

    var originalSource: Source? = null
        private set

    private var originalSeries: Series? = null

    private var originalIndication: String? = null


    private val editedSourceTitle = SimpleStringProperty("")

    private val editedIndication = SimpleStringProperty("")

    private val isSeriesOrPublishingDateSet = SimpleBooleanProperty()

    private val seriesAndPublishingDatePreview = SimpleStringProperty()

    private val showSourceSearchResult = SimpleBooleanProperty()

    private val sourceSearchResults: ObservableList<Source> = FXCollections.observableArrayList()

    private val referenceListPresenter: ReferencesListPresenter


    private var txtfldTitle: TextField by singleAssign()

    private var lstvwSearchResults: ListView<Source> by singleAssign()


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var eventBus: IEventBus


    init {
        AppComponent.component.inject(this)

        referenceListPresenter = ReferencesListPresenter(object : IReferencesListView {

            override fun showEntities(entities: List<Source>) {
                runLater { retrievedSourceSearchResultsOnUiThread(entities) }
            }

        }, searchEngine, router, clipboardService, deleteEntityService)
    }


    override val root = vbox {
        hbox {
            prefHeight = 20.0
            maxHeight = 70.0
            alignment = Pos.CENTER_LEFT
            prefWidthProperty().bind(this@vbox.widthProperty())

            label(messages["edit.item.source.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true
            }

            label {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true

                textProperty().bind(seriesAndPublishingDatePreview)
                visibleProperty().bind(isSeriesOrPublishingDateSet)
                FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

                cursor = Cursor.HAND
                setOnMouseClicked { seriesAndPublishingDatePreviewClicked(it) }
            }

            txtfldTitle = textfield(editedSourceTitle) {
                hgrow = Priority.ALWAYS

                promptText = messages["find.source.prompt.text"]

                textProperty().addListener { _, _, newValue -> enteredSourceTitleUpdated(newValue) }
                focusedProperty().addListener { _, _, newValue ->
                    textFieldTitleOrSearchResultsFocusedChanged(newValue, lstvwSearchResults.isFocused)

                    if(newValue) referenceListPresenter.searchReferences(text)
                }

                setOnKeyReleased { event ->
                    if(event.code == KeyCode.ENTER) {
                        createOrSelectSource()
                    }
                    else if(event.code == KeyCode.ESCAPE) {
                        clear()
                        hideSourceSearchResult()
                    }
                }

                hboxConstraints {
                    marginLeft = 4.0
                    marginRight = 4.0
                }
            }

            button {
                minHeight = 26.0
                maxHeight = minHeight
                minWidth = 26.0
                maxWidth = minWidth

                graphic = imageview(Icons.MoreVerticalIconPath) {
                    fitHeight = 26.0
                    isPreserveRatio = true

                    setImageTintColor(Color.GRAY)
                }

                setOnMouseClicked { buttonClicked(this, it) }
            }


            label(messages["edit.item.source.indication.label"]) {
                minWidth = Control.USE_PREF_SIZE
                useMaxWidth = true

                hboxConstraints {
                    marginLeft = 18.0
                    marginRight = 4.0
                }
            }

            textfield(editedIndication) {
                promptText = messages["source.indication.prompt.text"]

                prefWidth = 140.0

                textProperty().addListener { _, _, newValue -> enteredIndicationUpdated(newValue) }
            }


            vboxConstraints {
                marginBottom = 6.0
            }
        }

        lstvwSearchResults = listview(sourceSearchResults) {
            vgrow = Priority.ALWAYS
            minHeight = 100.0
            maxHeight = 200.0 // set maxHeight so that RichTextEditor doesn't get displayed over buttons bar

            visibleProperty().bind(showSourceSearchResult)
            FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

            cellFragment(SourceListCellFragment::class)

            focusedProperty().addListener { _, _, newValue -> textFieldTitleOrSearchResultsFocusedChanged(txtfldTitle.isFocused, newValue) }
            onDoubleClick { selectedSource(selectionModel.selectedItem) }

            contextmenu {
                item(messages["action.edit"]) {
                    action {
                        selectedItem?.let { referenceListPresenter.editReference(it) }
                    }
                }

                separator()

                item(messages["action.delete"]) {
                    action {
                        selectedItem?.let { referenceListPresenter.deleteReference(it) }
                    }
                }
            }
        }
    }

    private fun enteredSourceTitleUpdated(enteredSourceTitle: String) {
        if(sourceToEdit == null && enteredSourceTitle.isNotBlank()) { // user entered a title, but source is null -> create a new Source
            setSource(Source(enteredSourceTitle), null)
        }

        didTitleChange.value = enteredSourceTitle != originalSource?.title

        referenceListPresenter.searchReferences(enteredSourceTitle)
    }

    private fun enteredIndicationUpdated(enteredIndication: String) {
        didTitleChange.value = enteredIndication != originalIndication
    }

    private fun buttonClicked(node: Node, event: MouseEvent) {
       if(event.button == MouseButton.PRIMARY && event.clickCount == 1) {
           showContextMenu(node)
        }
    }

    private fun showContextMenu(node: Node) {
        val contextMenu = ContextMenu()

        contextMenu.item(messages["edit.item.source.field.edit.details.menu.item"]) {
            action { showEditSourceDialog() }
        }

        contextMenu.item(messages["edit.item.source.field.create.new.source.menu.item"]) {
            action { setSource(Source(""), null) }
        }

        contextMenu.item(messages["edit.item.source.field.remove.source.menu.item"]) {
            action { setSource(null, null) }
        }

        val screenBounds = node.localToScreen(node.boundsInLocal)
        contextMenu.show(node, screenBounds.minX, screenBounds.maxY)
    }


    fun setSourceToEdit(source: Source?, series: Series?, indication: String) {
        originalSource = source
        originalSeries = series

        originalIndication = indication
        editedIndication.value = indication

        setSource(source, series)
    }

    private fun selectedSource(source: Source?) {
        setSource(source, source?.series)

        txtfldTitle.impl_traverse(Direction.NEXT)
        runLater { hideSourceSearchResult() }
    }

    private fun setSource(source: Source?, series: Series?) {
        sourceToEdit = source
        seriesToEdit = series

        showSourcePreview(source, seriesToEdit)

        didEntityChange.value = sourceToEdit != originalSource || seriesToEdit != originalSeries
    }

    private fun showSourcePreview(source: Source?, series: Series?) {
        this.editedSourceTitle.value = source?.title ?: ""
        txtfldTitle.positionCaret(txtfldTitle.text.length)

        this.seriesAndPublishingDatePreview.value = source?.getSeriesAndPublishingDatePreview(series) ?: ""
        this.isSeriesOrPublishingDateSet.value = this.seriesAndPublishingDatePreview.value.isNullOrBlank() == false
    }

    private fun retrievedSourceSearchResultsOnUiThread(result: List<Source>) {
        sourceSearchResults.setAll(result)

        if(txtfldTitle.isFocused || lstvwSearchResults.isFocused) {
            showSourceSearchResult.value = true
        }
    }

    private fun createOrSelectSource() {
        if(sourceSearchResults.size > 0) {
            selectedSource(sourceSearchResults[0])
        }
        else if(sourceSearchResults.size == 0 && editedSourceTitle.value.isNotBlank()) {
            hideSourceSearchResult()
        }
    }

    private fun textFieldTitleOrSearchResultsFocusedChanged(titleHasFocus: Boolean, searchResultsHaveFocus: Boolean) {
        showSourceSearchResult.value = titleHasFocus || searchResultsHaveFocus
    }

    private fun hideSourceSearchResult() {
        showSourceSearchResult.value = false
    }

    private fun seriesAndPublishingDatePreviewClicked(event: MouseEvent) {
        if(event.button == MouseButton.PRIMARY) {
            showEditSourceDialog()
        }
    }

    private fun showEditSourceDialog() {
        eventBus.register(EventBusListener())

        router.showEditEntryReferenceView(sourceToEdit, seriesToEdit, txtfldTitle.text)
    }


    inner class EventBusListener {

        @Handler
        fun editingSourceDone(event: EditingSourceDoneEvent) {
            eventBus.unregister(this)

            if(event.didSaveSource) {
                runLater {
                    setSource(event.savedSource, event.savedSource?.series)
                }
            }
        }

    }

}
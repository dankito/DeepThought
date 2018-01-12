package net.dankito.deepthought.javafx.dialogs.entry.controls

import com.sun.javafx.scene.traversal.Direction
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Control
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.source.EditSourceDialog
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getSeriesAndPublishingDatePreview
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


class EditItemSourceField : View() {

    var sourceToEdit: Source? = null
        private set

    var seriesToEdit: Series? = null
        private set

    val didValueChange = SimpleBooleanProperty()

    private var originalSource: Source? = null

    private var originalSeries: Series? = null


    private val editedSourceTitle = SimpleStringProperty("")

    private val isSeriesOrPublishingDateSet = SimpleBooleanProperty()

    private val seriesAndPublishingDatePreview = SimpleStringProperty()

    private val showSourceSearchResult = SimpleBooleanProperty()

    private val sourceSearchResults: ObservableList<Source> = FXCollections.observableArrayList()

    private val referenceListPresenter: ReferencesListPresenter


    private var txtfldTitle: TextField by singleAssign()

    private var lstvwSearchResults: ListView<Source> by singleAssign()

    private var editSourceDialog: EditSourceDialog? = null


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var router: IRouter


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

                hboxConstraints {
                    marginRight = 4.0
                }
            }

            txtfldTitle = textfield(editedSourceTitle) {
                hgrow = Priority.ALWAYS

                promptText = messages["find.source.prompt.text"]

                textProperty().addListener { _, _, newValue -> referenceListPresenter.searchReferences(newValue) }
                focusedProperty().addListener { _, _, newValue -> textFieldTitleOrSearchResultsFocusedChanged(newValue, lstvwSearchResults.isFocused) }

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

        lstvwSearchResults = listview(sourceSearchResults) {
            vgrow = Priority.ALWAYS
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


    fun setSourceToEdit(source: Source?, series: Series?) {
        originalSource = source
        originalSeries = series

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

        didValueChange.value = sourceToEdit != originalSource || seriesToEdit != originalSeries
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
            // TODO: create Source
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
            if(editSourceDialog == null) {
                editSourceDialog = find(EditSourceDialog::class, mapOf(EditSourceDialog::source to (sourceToEdit ?: Source(""))))
                editSourceDialog?.show(messages["edit.item.summary.dialog.title"], owner = currentStage ) // TODO: add icon
            }
            else {
                editSourceDialog?.currentStage?.requestFocus()
            }
        }
    }

}
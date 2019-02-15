package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.SourceViewModel
import net.dankito.deepthought.javafx.ui.controls.cell.SourceListCellFragment
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.SourcesListPresenter
import net.dankito.deepthought.ui.view.ISourcesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.*
import javax.inject.Inject


class SourcesListView : EntitiesListView(), ISourcesListView {

    private val presenter: SourcesListPresenter

    private val searchBar: SourcesSearchBar

    private lateinit var listViewSources: ListView<Source>


    private val sources: ObservableList<Source> = FXCollections.observableArrayList()

    private val sourceViewModel = SourceViewModel()

    private var lastSelectedSource: Source? = null


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    init {
        AppComponent.component.inject(this)

        presenter = SourcesListPresenter(this, searchEngine, router, dialogService, clipboardService, deleteEntityService)
        searchBar = SourcesSearchBar(this)
    }


    override fun onDock() {
        super.onDock()

        presenter.viewBecomesVisible()
    }

    override fun onUndock() {
        presenter.viewGetsHidden()

        super.onUndock()
    }


    override val root = vbox {
        add(searchBar.root)

        listViewSources = listview<Source>(sources) {
            vgrow = Priority.ALWAYS

            selectionModel.selectionMode = SelectionMode.MULTIPLE

            cellFragment(SourceListCellFragment::class)

            bindSelected(sourceViewModel)

            // don't know why but selectionModel.selectedItemProperty() doesn't work reliably. Another source gets selected but selectedItemProperty() doesn't fire this change
            selectionModel.selectedIndexProperty().addListener { _, _, newValue -> sourceSelected(newValue.toInt()) }

            var currentMenu: ContextMenu? = null
            setOnContextMenuRequested { event ->
                currentMenu?.hide()

                currentMenu = createContextMenuForItems(this.selectionModel.selectedItems)
                currentMenu?.show(this, event.screenX, event.screenY)
            }
        }
    }

    private fun createContextMenuForItems(sources: List<Source>): ContextMenu? {
        if (sources.isEmpty()) {
            return null
        }
        else if (sources.size == 1) {
            return createContextMenuForSingleSource(sources[0])
        }
        else {
            return createContextMenuForMultipleSources(sources)
        }
    }

    private fun createContextMenuForSingleSource(source: Source): ContextMenu {
        val contextMenu = ContextMenu()

        contextMenu.item(messages["action.edit"]) {
            action {
                presenter.editSource(source)
            }
        }

        if(source.url.isNullOrBlank() == false) {
            contextMenu.item(messages["context.menu.item.copy.url.to.clipboard"]) {
                action {
                    presenter.copySourceUrlToClipboard(source)
                }
            }
        }

        separator()

        contextMenu.item(messages["action.delete"]) {
            action {
                presenter.confirmDeleteSourceAsync(source)
            }
        }

        return contextMenu
    }

    private fun createContextMenuForMultipleSources(sources: List<Source>): ContextMenu {
        val contextMenu = ContextMenu()

        contextMenu.item(messages["action.edit"]) {
            action {
                sources.forEach { presenter.editSource(it) }
            }
        }

        separator()

        contextMenu.item(messages["action.delete"]) {
            action {
                presenter.confirmDeleteSourcesAsync(sources)
            }
        }

        return contextMenu
    }


    fun viewCameIntoView() {
        if(lastSelectedSource == null && sources.isEmpty()) { // tab gets displayed for first time
            searchEntities(Search.EmptySearchTerm)
        }
        else {
            showItemsForLastSelectedEntity()
        }
    }

    private fun sourceSelected(selectedSourceIndex: Int) {
        if(selectedSourceIndex >= 0 && selectedSourceIndex < sources.size) {
            sourceSelected(sources[selectedSourceIndex])
        }
    }

    private fun sourceSelected(selectedSource: Source?) {
        lastSelectedSource = selectedSource

        showItemsForLastSelectedEntity()
    }

    private fun showItemsForLastSelectedEntity() {
        val selectedSource = lastSelectedSource

        if(selectedSource != null) {
            presenter.showItemsForSource(selectedSource)
        }
        else if(sources.isNotEmpty()) {
            listViewSources.selectionModel.select(0) // this will select first source in list and then show its items
        }
    }


    override fun searchEntities(query: String) {
        presenter.searchSources(query)
    }


    /*          ITagsListView implementation            */

    override fun showEntities(entities: List<Source>) {
        runLater { this.sources.setAll(entities) }
    }

}
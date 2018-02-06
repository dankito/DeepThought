package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.SourceViewModel
import net.dankito.deepthought.javafx.service.extensions.findClickedListCell
import net.dankito.deepthought.javafx.ui.controls.cell.SourceListCellFragment
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ReferencesListPresenter
import net.dankito.deepthought.ui.view.IReferencesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IClipboardService
import tornadofx.*
import javax.inject.Inject


class SourcesListView : EntitiesListView(), IReferencesListView {

    private val presenter: ReferencesListPresenter

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
    protected lateinit var clipboardService: IClipboardService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService


    init {
        AppComponent.component.inject(this)

        presenter = ReferencesListPresenter(this, searchEngine, router, clipboardService, deleteEntityService)
        searchBar = SourcesSearchBar(this)

        searchEngine.addInitializationListener {
            searchEntities(Search.EmptySearchTerm)
        }
    }

    override fun onUndock() {
        presenter.cleanUp()
        super.onUndock()
    }


    override val root = vbox {
        add(searchBar.root)

        listViewSources = listview<Source>(sources) {
            vgrow = Priority.ALWAYS

            cellFragment(SourceListCellFragment::class)

            bindSelected(sourceViewModel)

            // don't know why but selectionModel.selectedItemProperty() doesn't work reliably. Another source gets selected but selectedItemProperty() doesn't fire this change
            selectionModel.selectedIndexProperty().addListener { _, _, newValue -> sourceSelected(newValue.toInt()) }

            var currentMenu: ContextMenu? = null
            setOnContextMenuRequested { event ->
                currentMenu?.hide()

                val listCell = event.pickResult?.findClickedListCell<Source>()
                listCell?.item?.let { clickedItem ->
                    currentMenu = createContextMenuForItem(clickedItem)
                    currentMenu?.show(this, event.screenX, event.screenY)
                }
            }
        }
    }

    private fun createContextMenuForItem(clickedItem: Source): ContextMenu {
        val contextMenu = ContextMenu()

        contextMenu.item(messages["action.edit"]) {
            action {
                presenter.editReference(clickedItem)
            }
        }

        if(clickedItem.url.isNullOrBlank() == false) {
            contextMenu.item(messages["context.menu.item.copy.url.to.clipboard"]) {
                action {
                    presenter.copyReferenceUrlToClipboard(clickedItem)
                }
            }
        }

        separator()

        contextMenu.item(messages["action.delete"]) {
            action {
                presenter.deleteReference(clickedItem)
            }
        }

        return contextMenu
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

    fun showItemsForLastSelectedEntity() {
        val selectedSource = lastSelectedSource

        if(selectedSource != null) {
            presenter.showItemsForSource(selectedSource)
        }
        else if(sources.isNotEmpty()) {
            listViewSources.selectionModel.select(0) // this will select first source in list and then show its items
        }
    }


    override fun searchEntities(query: String) {
        presenter.searchReferences(query)
    }


    /*          ITagsListView implementation            */

    override fun showEntities(entities: List<Source>) {
        runLater { this.sources.setAll(entities) }
    }

}
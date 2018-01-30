package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.SourceViewModel
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
            bindSelected(sourceViewModel)

            vgrow = Priority.ALWAYS

            // don't know why but selectionModel.selectedItemProperty() doesn't work reliably. Another source gets selected but selectedItemProperty() doesn't fire this change
            selectionModel.selectedIndexProperty().addListener { _, _, newValue -> sourceSelected(newValue.toInt()) }

            contextmenu {
                item(messages["action.edit"]) {
                    isDisable = true
                    action {
                        selectedItem?.let { presenter.editReference(it) }
                    }
                }

                separator()

                item(messages["action.delete"]) {
                    action {
                        selectedItem?.let { presenter.deleteReference(it) }
                    }
                }
            }
        }
    }


    private fun sourceSelected(selectedSourceIndex: Int) {
        if(selectedSourceIndex >= 0 && selectedSourceIndex < sources.size) {
            sourceSelected(sources[selectedSourceIndex])
        }
    }

    private fun sourceSelected(selectedSource: Source?) {
        if(selectedSource != null) {
            presenter.showEntriesForReference(selectedSource)
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
package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.TagViewModel
import net.dankito.deepthought.model.AllCalculatedTags
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import javax.inject.Inject


class TagsListView : EntitiesListView(), ITagsListView {

    private val presenter: TagsListPresenter

    private val searchBar: TagsSearchBar

    private lateinit var tableTags: TableView<Tag>


    private val tags: ObservableList<Tag> = FXCollections.observableArrayList()

    private val tagViewModel = TagViewModel()


    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var searchResultsUtil: TagsSearchResultsUtil

    @Inject
    protected lateinit var tagService: TagService

    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var allCalculatedTags: AllCalculatedTags


    init {
        AppComponent.component.inject(this)

        presenter = TagsListPresenter(this, allCalculatedTags, searchEngine, searchResultsUtil, tagService, deleteEntityService, dialogService, router)
        searchBar = TagsSearchBar(this)

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

        tableTags = tableview<Tag> {
            column(messages["tag.column.header.name"], Tag::displayText) {
                isResizable = true
                makeEditable()
                setSortable(false)
                tableViewProperty().addListener { _, _, newValue ->
                    if(newValue != null) {
                        prefWidthProperty().bind(newValue.widthProperty().subtract(2)) // subtract(2): otherwise a useless scrollbar would be displayed
                    }
                }
            }

            // TODO: add filter column
//            column("") {
//                isResizable = false
//                minWidth = 35.0
//                maxWidth = 35.0
//            }

            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

            items = tags

            bindSelected(tagViewModel)

            vgrow = Priority.ALWAYS

            selectionModel.selectedItemProperty().addListener { _, _, newValue -> tagSelected(newValue) }

            contextmenu {
                item(messages["action.edit"]) {
                    isDisable = true
                    action {
                        selectedItem?.let { presenter.editTag(it) }
                    }
                }

                separator()

                item(messages["action.delete"]) {
                    action {
                        selectedItem?.let { presenter.deleteTagAsync(it) }
                    }
                }
            }
        }
    }


    private fun tagSelected(selectedTag: Tag?) {
        if(selectedTag != null) {
            presenter.showEntriesForTag(selectedTag)
        }
        else {
//            presenter.clearSelectedTag() // TODO
        }
    }

    override fun searchEntities(query: String) {
        presenter.searchTags(query)
    }


    /*          ITagsListView implementation            */

    override fun showEntities(entities: List<Tag>) {
        runLater { this.tags.setAll(entities) }
    }

    override fun updateDisplayedTags() {
        runLater { tableTags.items.invalidate() }
    }

}
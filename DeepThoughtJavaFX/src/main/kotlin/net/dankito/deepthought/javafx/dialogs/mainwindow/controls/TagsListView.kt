package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.collections.FXCollections
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.TagViewModel
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.TagsListPresenter
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.search.ISearchEngine
import tornadofx.*
import javax.inject.Inject


class TagsListView : View(), ITagsListView {

    private val searchBar: TagsSearchBar

    private val presenter: TagsListPresenter


    private val tags = FXCollections.observableArrayList<Tag>()

    private val tagViewModel = TagViewModel()


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    init {
        AppComponent.component.inject(this)

        presenter = TagsListPresenter(this, router, searchEngine)
        searchBar = TagsSearchBar(presenter)

        presenter.getAndShowAllEntities()
    }

    override fun onUndock() {
        presenter.cleanUp()
        super.onUndock()
    }


    override val root = vbox {
        add(searchBar.root)

        tableview<Tag> {
            column("name", Tag::displayText) {
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

            selectionModel.selectedItemProperty().addListener { _, _, newValue -> presenter.showEntriesForTag(newValue) }
        }
    }


    /*          ITagsListView implementation            */

    override fun showTags(tags: List<Tag>) {
        runLater { this.tags.setAll(tags) }
    }

}
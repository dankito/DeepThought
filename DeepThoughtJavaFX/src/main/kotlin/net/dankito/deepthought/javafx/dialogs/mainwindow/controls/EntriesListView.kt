package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.EntryViewModel
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.referencePreview
import net.dankito.deepthought.model.extensions.tagsPreview
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import tornadofx.*
import java.text.DateFormat
import javax.inject.Inject


class EntriesListView : EntitiesListView(), IEntriesListView {

    companion object {
        private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG)
    }


    private val entryModel = EntryViewModel()

    private val entries = LazyLoadingObservableList<Item>()

    private val searchBar: EntriesSearchBar

    private var tableEntries: TableView<Item> by singleAssign()

    var statusBar: StatusBar? = null

    private val presenter: EntriesListPresenter


    @Inject
    protected lateinit var deleteEntityService: DeleteEntityService

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var clipboardService: IClipboardService


    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)
        searchBar = EntriesSearchBar(this, presenter)

        (router as? JavaFXRouter)?.entriesListView = this // TODO: this is bad code design

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

        tableEntries = tableview<Item>(entries) {
            column(messages["item.column.header.index"], Item::itemIndex).prefWidth(46.0)
            column(messages["item.column.header.source"], Item::referencePreview).weightedWidth(4.0)
            column(messages["item.column.header.preview"], Item::preview).weightedWidth(4.0)
            column(messages["item.column.header.tags"], Item::tagsPreview).weightedWidth(2.0)
    //        column(messages["item.column.header.created"], stringBinding(Item::createdOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)
    //        column(messages["item.column.header.modified"], stringBinding(Item::modifiedOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)

            columnResizePolicy = SmartResize.POLICY

            bindSelected(entryModel)

            vgrow = Priority.ALWAYS

            onDoubleClick {
                selectionModel.selectedItem?.let { router.showEditEntryView(it) }
            }

            contextmenu {
                item(messages["context.menu.item.copy.url.to.clipboard"]) {
                    action {
                        selectedItem?.let { presenter.copyReferenceUrlToClipboard(it) }
                    }
                }

                item(messages["context.menu.item.copy.item.to.clipboard"]) {
                    action {
                        selectedItem?.let { presenter.copyItemToClipboard(it) }
                    }
                }

                separator()

                item(messages["context.menu.item.delete"]) {
                    action {
                        selectedItem?.let { askIfShouldDeleteEntry(it) }
                    }
                }
            }
        }
    }

    private fun askIfShouldDeleteEntry(item: Item) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.item")) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                presenter.deleteEntry(item)
            }
        }
    }


    override fun searchEntities(query: String) {
        presenter.searchEntries(query)
    }


    /*          IEntriesListView implementation            */

    override fun showEntities(entities: List<Item>) {
        runLater {
            entries.setAll(entities)
            tableEntries.refresh() // necessary when count items stays the same (e.g. when an item has been updated)

            statusBar?.showCountDisplayedEntriesOnUiThread(entities.size)
        }
    }

    override fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>) {
        presenter.showEntriesForTag(tag, tagsFilter)
    }

}
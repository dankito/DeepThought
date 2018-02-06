package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.scene.control.ContextMenu
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.ItemItemViewModel
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.service.extensions.findClickedTableRow
import net.dankito.deepthought.javafx.ui.controls.IItemsListViewJavaFX
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.sourcePreview
import net.dankito.deepthought.model.extensions.tagsPreview
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ItemsListPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton
import tornadofx.*
import java.text.DateFormat
import javax.inject.Inject


class ItemsListView : EntitiesListView(), IItemsListViewJavaFX {

    companion object {
        private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG)
    }


    private val itemModel = ItemItemViewModel()

    private val items = LazyLoadingObservableList<Item>()

    private val searchBar: ItemsSearchBar

    private var tableItems: TableView<Item> by singleAssign()

    var statusBar: StatusBar? = null

    private val presenter: ItemsListPresenter


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

    @Inject
    protected lateinit var dataManager: DataManager


    init {
        AppComponent.component.inject(this)

        presenter = ItemsListPresenter(this, router, searchEngine, deleteEntityService, clipboardService)
        searchBar = ItemsSearchBar(this, presenter, dataManager)

        (router as? JavaFXRouter)?.itemsListView = this // TODO: this is bad code design

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

        tableItems = tableview<Item>(items) {
            column(messages["item.column.header.index"], Item::itemIndex).prefWidth(46.0)
            column(messages["item.column.header.source"], Item::sourcePreview).weightedWidth(4.0)
            column(messages["item.column.header.preview"], Item::preview).weightedWidth(4.0)
            column(messages["item.column.header.tags"], Item::tagsPreview).weightedWidth(2.0)
    //        column(messages["item.column.header.created"], stringBinding(Item::createdOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)
    //        column(messages["item.column.header.modified"], stringBinding(Item::modifiedOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)

            columnResizePolicy = SmartResize.POLICY

            bindSelected(itemModel)

            vgrow = Priority.ALWAYS

            onDoubleClick {
                selectionModel.selectedItem?.let { router.showEditItemView(it) }
            }

            var currentMenu: ContextMenu? = null
            setOnContextMenuRequested { event ->
                currentMenu?.hide()

                val tableRow = event.pickResult?.findClickedTableRow<Item>()
                tableRow?.item?.let { clickedItem ->
                    currentMenu = createContextMenuForItem(clickedItem)
                    currentMenu?.show(this, event.screenX, event.screenY)
                }
            }

            contextmenu {

            }
        }
    }

    private fun createContextMenuForItem(item: Item): ContextMenu {
        val contextMenu = ContextMenu()

        if(item.source?.url.isNullOrBlank() == false) {
            contextMenu.item(messages["context.menu.item.copy.url.to.clipboard"]) {
                action { presenter.copySourceUrlToClipboard(item) }
            }
        }

        contextMenu.item(messages["context.menu.item.copy.item.to.clipboard"]) {
            action { presenter.copyItemToClipboard(item) }
        }

        contextMenu.separator()

        contextMenu.item(messages["context.menu.item.delete"]) {
            action { askIfShouldDeleteItem(item) }
        }

        return contextMenu
    }

    private fun askIfShouldDeleteItem(item: Item) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.item")) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                presenter.deleteItem(item)
            }
        }
    }


    fun createNewItem() {
        presenter.createItem()
    }


    override fun searchEntities(query: String) {
        presenter.searchItems(query)
    }


    /*          IItemsListView implementation            */

    override fun showEntities(entities: List<Item>) {
        runLater {
            items.setAll(entities)
            tableItems.refresh() // necessary when count items stays the same (e.g. when an item has been updated)

            statusBar?.showCountDisplayedItemsOnUiThread(entities.size)
        }
    }

    override fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>) {
        presenter.showItemsForTag(tag, tagsFilter)
    }

    override fun showItemsForSource(source: Source) {
        presenter.showItemsForSource(source)
    }

}
package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import com.sun.javafx.scene.control.skin.TableHeaderRow
import com.sun.javafx.scene.control.skin.TableViewSkinBase
import javafx.scene.control.ContextMenu
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.util.Callback
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.ItemItemViewModel
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.service.extensions.findClickedTableRow
import net.dankito.deepthought.javafx.ui.controls.IItemsListViewJavaFX
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.sourcePreviewOrSummary
import net.dankito.deepthought.model.extensions.tagsPreview
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.ItemsListPresenter
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.search.FieldName
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.Search
import net.dankito.service.search.util.SortOption
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.ConfirmationDialogButton
import net.dankito.utils.ui.dialogs.IDialogService
import tornadofx.*
import java.text.DateFormat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


class ItemsListView : EntitiesListView(), IItemsListViewJavaFX {

    companion object {
        private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
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

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        AppComponent.component.inject(this)

        presenter = ItemsListPresenter(this, router, searchEngine, deleteEntityService, clipboardService, threadPool)
        searchBar = ItemsSearchBar(this, presenter, dataManager)

        (router as? JavaFXRouter)?.itemsListView = this // TODO: this is bad code design

        runLater { // wait till UI is initialized before search index
            searchEngine.addInitializationListener {
                searchEntities(Search.EmptySearchTerm)
            }
        }
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

        tableItems = tableview(items) {
            column(messages["item.column.header.index"], Item::itemIndex) {
                prefWidth(46.0)

                isVisible = false
                isSortable = false
            }

            val sourcePreviewColumn = column(messages["item.column.header.source"], Item::sourcePreviewOrSummary) {
                prefWidth(350)

                id = FieldName.ItemPreviewForSorting
            }
            val contentPreviewColumn = column(messages["item.column.header.preview"], Item::preview) {
                prefWidth(350)

                id = FieldName.ItemSourcePreviewForSorting
            }

            column(messages["item.column.header.tags"], Item::tagsPreview).prefWidth(196).isSortable = false

            val createdColumn = column(messages["item.column.header.created"], Item::createdOn) {
                prefWidth(130)

                id = FieldName.ItemCreated

                isVisible = false

                cellFormat { text = dateTimeFormat.format(it) }
            }
            val modifiedColumn = column(messages["item.column.header.modified"], Item::modifiedOn) {
                prefWidth(130)

                id = FieldName.ModifiedOn

                isVisible = false

                cellFormat { text = dateTimeFormat.format(it) }
            }

            isTableMenuButtonVisible = true

            sortPolicyProperty().set(Callback<TableView<Item>, Boolean> {
                true
            })

            setHeaderClickListeners(this, sourcePreviewColumn, contentPreviewColumn, createdColumn, modifiedColumn)

            columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY

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

    private fun setHeaderClickListeners(tableView: TableView<Item>, vararg columns: TableColumn<Item, *>) {
        tableView.skinProperty().addListener { _, _, newValue ->
            Timer().schedule(2 * 1000) {
                // if called immediately getTableHeaderRow() returns null -> wait some time till header row is set
                runLater {
                    (newValue as? TableViewSkinBase<*, *, *, *, *, *>)?.getTableHeaderRow()?.let { headerRow ->
                        columns.forEach { column ->
                            setHeaderClickListener(tableView, headerRow, column)
                        }
                    }
                }
            }
        }
    }

    private fun setHeaderClickListener(tableView: TableView<Item>, headerRow: TableHeaderRow, column: TableColumn<Item, *>) {
        headerRow.getColumnHeaderFor(column)?.let { columnHeader ->
            columnHeader.setOnMouseClicked { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                    val sortOptions = tableView.sortOrder.map { SortOption(it.id, it.sortType == TableColumn.SortType.ASCENDING) }
                    presenter.setSortOptionsAsync(sortOptions)
                }
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
                presenter.deleteItemAsync(item)
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
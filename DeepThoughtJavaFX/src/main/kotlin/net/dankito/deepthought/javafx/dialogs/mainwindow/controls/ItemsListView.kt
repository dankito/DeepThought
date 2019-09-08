package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import com.sun.javafx.scene.control.skin.TableColumnHeader
import com.sun.javafx.scene.control.skin.TableViewSkinBase
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import javafx.util.Callback
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.ItemItemViewModel
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.javafx.ui.controls.IItemsListViewJavaFX
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
import net.dankito.utils.javafx.util.LazyLoadingObservableList
import net.dankito.utils.ui.IClipboardService
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

        presenter = ItemsListPresenter(this, router, searchEngine, deleteEntityService, dialogService, clipboardService, threadPool)
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

                id = FieldName.ItemSourcePreviewForSorting
            }
            val contentPreviewColumn = column(messages["item.column.header.preview"], Item::preview) {
                prefWidth(350)

                id = FieldName.ItemPreviewForSorting
            }

            column(messages["item.column.header.tags"], Item::tagsPreview).prefWidth(196).isSortable = false

            val createdColumn = column(messages["item.column.header.created"], Item::createdOn) {
                prefWidth(130)

                id = FieldName.ItemCreated

                isVisible = false

                cellFormat { text = dateTimeFormat.format(it) }

                AddColumnSorterWhenColumnBecomesVisible(this@ItemsListView, tableView, this)
            }
            val modifiedColumn = column(messages["item.column.header.modified"], Item::modifiedOn) {
                prefWidth(130)

                id = FieldName.ModifiedOn

                isVisible = false

                cellFormat { text = dateTimeFormat.format(it) }

                AddColumnSorterWhenColumnBecomesVisible(this@ItemsListView, tableView, this)
            }

            selectionModel.selectionMode = SelectionMode.MULTIPLE

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

            setOnKeyReleased { event ->
                handleKeyReleaseEvent(this, event)
            }

            var currentMenu: ContextMenu? = null
            setOnContextMenuRequested { event ->
                currentMenu?.hide()

                currentMenu = createContextMenuForItems(this.selectionModel.selectedItems)
                currentMenu?.show(this, event.screenX, event.screenY)
            }

            contextmenu {

            }
        }
    }

    private fun setHeaderClickListeners(tableView: TableView<Item>, vararg columns: TableColumn<Item, *>) {
        tableView.skinProperty().addListener { _, _, newValue ->
            setHeaderClickListenersDelayed(tableView, newValue, *columns)
        }
    }

    private fun setHeaderClickListenersDelayed(tableView: TableView<Item>, tableViewSkin: Skin<*>, vararg columns: TableColumn<Item, *>) {
        Timer().schedule(1000) { // at this point getTableHeaderRow() is null or columnHeader is not added yet to header row -> wait some time
            runLater {
                setHeaderClickListeners(tableView, tableViewSkin, *columns)
            }
        }
    }

    private fun setHeaderClickListeners(tableView: TableView<Item>, tableViewSkin: Skin<*>, vararg columns: TableColumn<Item, *>) {
        (tableViewSkin as? TableViewSkinBase<*, *, *, *, *, *>)?.getTableHeaderRow()?.let { headerRow ->
            columns.forEach { column ->
                headerRow.getColumnHeaderFor(column)?.let { columnHeader ->
                    sortColumnHeaderOnClick(tableView, columnHeader)
                }
            }
        }
    }

    private fun sortColumnHeaderOnClick(tableView: TableView<Item>, columnHeader: TableColumnHeader) {
        columnHeader.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                val sortOptions = tableView.sortOrder.map { SortOption(it.id, it.sortType == TableColumn.SortType.ASCENDING) }
                presenter.setSortOptionsAsync(sortOptions)
            }
        }
    }


    private fun handleKeyReleaseEvent(tableView: TableView<Item>, event: KeyEvent) {
        if (event.code == KeyCode.ENTER) {
            handleEnterKeyReleased(tableView.selectionModel.selectedItems)
        }
        else if (event.code == KeyCode.DELETE) {
            handleDeleteKeyReleased(tableView.selectionModel.selectedItems)
        }
    }

    private fun handleEnterKeyReleased(selectedItems: List<Item>) {
        selectedItems.forEach { item ->
            presenter.showItem(item)
        }
    }

    private fun handleDeleteKeyReleased(selectedItems: List<Item>) {
        if (selectedItems.size == 1) {
            presenter.confirmDeleteItemAsync(selectedItems[0])
        }
        else if (selectedItems.isNotEmpty()) {
            presenter.confirmDeleteItemsAsync(selectedItems)
        }
    }


    private fun createContextMenuForItems(items: List<Item>): ContextMenu? {
        if (items.isEmpty()) {
            return null
        }
        else if (items.size == 1) {
            return createContextMenuForSingleItem(items[0])
        }
        else {
            return createContextMenuForMultipleItems(items)
        }
    }

    private fun createContextMenuForSingleItem(item: Item): ContextMenu {
        val contextMenu = ContextMenu()

        if(item.source?.url.isNullOrBlank() == false) {
            contextMenu.item(messages["context.menu.item.copy.url.to.clipboard"]) {
                action { presenter.copySourceUrlToClipboard(item) }
            }
        }

        contextMenu.item(messages["context.menu.item.copy.item.to.clipboard"]) {
            action { presenter.copyItemToClipboard(item) }
        }

        contextMenu.item(messages["context.menu.item.copy.item.content.as.html.to.clipboard"]) {
            action { presenter.copyItemContentAsHtmlToClipboard(item) }
        }

        contextMenu.separator()

        contextMenu.item(messages["context.menu.item.delete"]) {
            action { presenter.confirmDeleteItemAsync(item) }
        }

        return contextMenu
    }

    private fun createContextMenuForMultipleItems(items: List<Item>): ContextMenu {
        val contextMenu = ContextMenu()

        contextMenu.item(messages["context.menu.item.delete"]) {
            action { presenter.confirmDeleteItemsAsync(items) }
        }

        return contextMenu
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


    private class AddColumnSorterWhenColumnBecomesVisible(private val itemsListView: ItemsListView, private val tableView: TableView<Item>,
                                                          private val column: TableColumn<Item, *>) : ChangeListener<Boolean> {

        init {
            column.visibleProperty().addListener(this)
        }

        override fun changed(observable: ObservableValue<out Boolean>, oldValue: Boolean, newValue: Boolean) {
            if (newValue) {
                column.visibleProperty().removeListener(this)

                itemsListView.setHeaderClickListenersDelayed(tableView, tableView.skin, column)
            }
        }

    }

}
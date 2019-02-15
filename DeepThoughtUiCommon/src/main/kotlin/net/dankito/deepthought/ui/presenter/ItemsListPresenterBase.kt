package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService


abstract class ItemsListPresenterBase(protected val deleteEntityService: DeleteEntityService, protected val clipboardService: IClipboardService,
                                      protected val router: IRouter, protected val threadPool: IThreadPool) {


    fun showItem(item: Item) {
        router.showEditItemView(item)
    }

    fun copySourceUrlToClipboard(item: Item) {
        item.source?.url?.let {
            clipboardService.copyUrlToClipboard(it)
        }
    }

    fun copyItemToClipboard(item: Item) {
        clipboardService.copyItemToClipboard(item, item.tags, item.source, item.source?.series)
    }


    fun deleteItemsAsync(items: Collection<Item>) {
        threadPool.runAsync {
            ArrayList(items).forEach {
                deleteItem(it)
            }
        }
    }

    fun deleteItemAsync(item: Item) {
        threadPool.runAsync {
            deleteItem(item)
        }
    }

    fun deleteItem(item: Item) {
        deleteEntityService.deleteItemAsync(item)
    }

}
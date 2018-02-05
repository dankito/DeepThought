package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService


abstract class ItemsListPresenterBase(private val deleteEntityService: DeleteEntityService, private val clipboardService: IClipboardService, private val router: IRouter) {


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

    fun deleteItem(item: Item) {
        deleteEntityService.deleteItemAsync(item)
    }

}
package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IClipboardService
import net.dankito.utils.ui.dialogs.ConfirmationDialogButton
import net.dankito.utils.ui.dialogs.IDialogService


abstract class ItemsListPresenterBase(protected val deleteEntityService: DeleteEntityService, protected val dialogService: IDialogService,
                                      protected val clipboardService: IClipboardService, protected val router: IRouter, protected val threadPool: IThreadPool) {


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

    fun copyItemContentAsHtmlToClipboard(item: Item) {
        clipboardService.copyItemContentAsHtmlToClipboard(item)
    }


    fun confirmDeleteItemsAsync(items: List<Item>) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.items", items.size)) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteItemsAsync(items)
            }
        }
    }

    fun deleteItemsAsync(items: Collection<Item>) {
        threadPool.runAsync {
            ArrayList(items).forEach {
                deleteItem(it)
            }
        }
    }

    fun confirmDeleteItemAsync(item: Item) {
        dialogService.showConfirmationDialog(dialogService.getLocalization().getLocalizedString("alert.message.really.delete.item")) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                deleteItemAsync(item)
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
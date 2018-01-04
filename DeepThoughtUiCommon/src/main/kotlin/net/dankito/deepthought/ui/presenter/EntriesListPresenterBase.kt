package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService


abstract class EntriesListPresenterBase(private val deleteEntityService: DeleteEntityService, private val clipboardService: IClipboardService, private val router: IRouter) {


    fun showEntry(item: Item) {
        router.showEditEntryView(item)
    }

    fun copyReferenceUrlToClipboard(item: Item) {
        item.source?.url?.let {
            clipboardService.copyUrlToClipboard(it)
        }
    }

    fun copyItemToClipboard(item: Item) {
        clipboardService.copyEntryToClipboard(item, item.source, item.source?.series)
    }

    fun deleteEntry(item: Item) {
        deleteEntityService.deleteEntryAsync(item)
    }

}
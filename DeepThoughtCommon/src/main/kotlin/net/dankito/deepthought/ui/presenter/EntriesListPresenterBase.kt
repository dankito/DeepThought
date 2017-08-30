package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.DeleteEntityService
import net.dankito.utils.ui.IClipboardService


abstract class EntriesListPresenterBase(private val deleteEntityService: DeleteEntityService, private val clipboardService: IClipboardService, private val router: IRouter) {


    fun showEntry(entry: Entry) {
        router.showEditEntryView(entry)
    }

    fun copyReferenceUrlToClipboard(entry: Entry) {
        entry.reference?.let {
            clipboardService.copyReferenceUrlToClipboard(it)
        }
    }

    fun deleteEntry(entry: Entry) {
        deleteEntityService.deleteEntryAsync(entry)
    }

}
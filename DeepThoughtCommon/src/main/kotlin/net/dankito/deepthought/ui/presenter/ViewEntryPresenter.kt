package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.EntryField
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.utils.ui.IClipboardService


class ViewEntryPresenter(private val entryPersister: EntryPersister, private val clipboardService: IClipboardService, private var router: IRouter) {


    fun saveEntryExtractionResultAsync(result: EntryExtractionResult, callback: ((Boolean) -> Unit)? = null) {
        entryPersister.saveEntryAsync(result) { successful ->
            if(successful) {
                returnToPreviousView()
            }

            callback?.invoke(successful)
        }
    }


    fun editEntry(entry: Entry, field: EntryField? = null) {
        router.showEditEntryView(entry, field)
    }

    fun editEntry(article: ReadLaterArticle, field: EntryField? = null) {
        router.showEditEntryView(article, field)
    }

    fun editEntry(extractionResult: EntryExtractionResult, field: EntryField? = null) {
        router.showEditEntryView(extractionResult, field)
    }


    fun shareReferenceUrl(reference: Reference) {
        clipboardService.copyReferenceUrlToClipboard(reference)
    }

    fun shareEntry(entry: Entry, reference: Reference?) {
        clipboardService.copyEntryToClipboard(entry, reference)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
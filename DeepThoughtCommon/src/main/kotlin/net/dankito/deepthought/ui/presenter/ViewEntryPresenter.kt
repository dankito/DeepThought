package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.utils.ui.IClipboardService


class ViewEntryPresenter(private val entryPersister: EntryPersister, private val clipboardService: IClipboardService, private var router: IRouter) {


    fun saveEntryExtractionResult(result: EntryExtractionResult) {
        entryPersister.saveEntry(result)

        returnToPreviousView()
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
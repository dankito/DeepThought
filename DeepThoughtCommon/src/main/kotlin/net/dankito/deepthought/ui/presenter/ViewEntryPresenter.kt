package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService


class ViewEntryPresenter(entryService: EntryService, referenceService: ReferenceService, private var router: IRouter) {

    private val entryPersister = EntryPersister(entryService, referenceService)


    fun saveEntryExtractionResult(result: EntryExtractionResult) {
        entryPersister.saveEntry(result.entry, result.reference, result.tags)

        returnToPreviousView()
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
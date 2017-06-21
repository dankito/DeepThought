package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService


class ViewEntryPresenter(private var entry: Entry?, private var entryExtractionResult: EntryExtractionResult?,
                         entryService: EntryService, referenceService: ReferenceService, private var router: IRouter) {

    private val entryPersister = EntryPersister(entryService, referenceService)


    fun saveEntry() {
        entryExtractionResult?.let { result ->
            entryPersister.saveEntry(result.entry, result.reference, result.tags)
        }

        returnToPreviousView()
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
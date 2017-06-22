package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService


class ViewEntryPresenter(entryService: EntryService, referenceService: ReferenceService, tagService: TagService, private var router: IRouter) {

    private val entryPersister = EntryPersister(entryService, referenceService, tagService)


    fun saveEntryExtractionResult(result: EntryExtractionResult) {
        entryPersister.saveEntry(result)

        returnToPreviousView()
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.ui.IRouter
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService


class ViewEntryPresenter(private var entry: Entry?, private var entryExtractionResult: EntryExtractionResult?,
                         private var entryService: EntryService, private var referenceService: ReferenceService, private var router: IRouter) {

    fun saveEntry() {
        entryExtractionResult?.let { entryExtractionResult ->
            val entry = entryExtractionResult.entry

            entry.reference = createAndPersistReference(entryExtractionResult)

            entryService.persist(entry)
        }

        returnToPreviousView()
    }

    private fun createAndPersistReference(entryExtractionResult: EntryExtractionResult): Reference? {
        val reference = entryExtractionResult.reference

        if(reference != null) {
            referenceService.persist(reference)
        }

        return reference
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
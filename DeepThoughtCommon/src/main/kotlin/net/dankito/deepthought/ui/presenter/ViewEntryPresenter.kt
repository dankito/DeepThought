package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.newsreader.model.EntryExtractionResult


class ViewEntryPresenter(private val entryPersister: EntryPersister, private var router: IRouter) {


    fun saveEntryExtractionResult(result: EntryExtractionResult) {
        entryPersister.saveEntry(result)

        returnToPreviousView()
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
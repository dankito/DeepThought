package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister


class EditEntryPresenter(private val entryPersister: EntryPersister, private val router: IRouter) {


    fun saveEntry(entry: Entry, reference: Reference? = null, tags: Collection<Tag> = ArrayList()): Boolean {
        return entryPersister.saveEntry(entry, reference, tags)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
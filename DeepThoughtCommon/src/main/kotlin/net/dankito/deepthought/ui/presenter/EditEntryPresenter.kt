package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.utils.IThreadPool
import javax.inject.Inject


class EditEntryPresenter(private val entryPersister: EntryPersister, private val router: IRouter) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun createReference() {
        router.showCreateReferenceView()
    }

    fun editReference(reference: Reference, ofEntry: Entry?) {
        router.showEditEntryReferenceView(reference, ofEntry)
    }

    fun saveEntryAsync(entry: Entry, reference: Reference? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        entryPersister.saveEntryAsync(entry, reference, tags, callback)
    }


    fun returnToPreviousView() {
        router.returnToPreviousView()
    }

}
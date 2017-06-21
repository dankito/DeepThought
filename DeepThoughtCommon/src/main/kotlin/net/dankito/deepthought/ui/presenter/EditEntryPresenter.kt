package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.presenter.util.EntryPersister
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService


class EditEntryPresenter(entryService: EntryService, referenceService: ReferenceService, tagService: TagService) {

    private val entryPersister = EntryPersister(entryService, referenceService, tagService)


    fun saveEntry(entry: Entry, reference: Reference? = null, tags: List<Tag> = ArrayList()): Boolean {
        return entryPersister.saveEntry(entry, reference, tags)
    }

}
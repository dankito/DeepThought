package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService


class EntryPersister(private val entryService: EntryService, private val referenceService: ReferenceService) {

    fun saveEntry(entry: Entry, reference: Reference? = null, tags: List<Tag> = ArrayList()): Boolean {
        // by design at this stage there's no unpersisted tag -> set them directly on entry so that their ids get saved on entry with persist(entry) / update(entry)
        entry.setTags(tags)

        reference?.let { reference ->
            if(reference.isPersisted() == false) {
                referenceService.persist(reference)
            }
        }

        entry.reference = reference

        if(entry.isPersisted() == false) {
            entryService.persist(entry)
        }
        else {
            entryService.update(entry)
        }

        for(tag in tags) {
            // TODO: update Tag
        }

        return true
    }

}
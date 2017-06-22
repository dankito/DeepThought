package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.newsreader.model.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService


class EntryPersister(private val entryService: EntryService, private val referenceService: ReferenceService, private val tagService: TagService) {

    fun saveEntry(result: EntryExtractionResult): Boolean {
        return saveEntry(result.entry, result.reference, result.tags)
    }

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
            tagService.update(tag) // TODO: check if tag needs an update
        }

        return true
    }

}
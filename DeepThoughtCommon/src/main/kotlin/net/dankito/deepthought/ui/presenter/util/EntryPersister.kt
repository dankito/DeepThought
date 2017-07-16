package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.ReferenceService
import net.dankito.service.data.TagService


class EntryPersister(private val entryService: EntryService, private val referenceService: ReferenceService, private val tagService: TagService) {

    fun saveEntry(result: EntryExtractionResult): Boolean {
        return saveEntry(result.entry, result.reference, result.tags)
    }

    fun saveEntry(entry: Entry, reference: Reference? = null, tags: Collection<Tag> = ArrayList<Tag>()): Boolean {
        // by design at this stage there's no unpersisted tag -> set them directly on entry so that their ids get saved on entry with persist(entry) / update(entry)
        val removedTags = ArrayList(entry.tags)
        removedTags.removeAll(tags)

        entry.setAllTags(tags)

        removedTags.forEach { tagService.update(it) }


        reference?.let { reference ->
            if(reference.isPersisted() == false) {
                referenceService.persist(reference)
            }
        }

        val previousReference = reference

        entry.reference = reference

        previousReference?.let { referenceService.update(it) }


        if(entry.isPersisted() == false) {
            entryService.persist(entry)
        }
        else {
            entryService.update(entry)
        }


        reference?.let { referenceService.update(reference) }

        for(tag in tags) {
            tagService.update(tag) // TODO: check if tag needs an update
        }

        return true
    }

}
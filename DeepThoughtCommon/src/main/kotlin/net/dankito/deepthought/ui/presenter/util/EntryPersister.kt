package net.dankito.deepthought.ui.presenter.util

import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.TagService
import net.dankito.utils.IThreadPool
import javax.inject.Inject


class EntryPersister(private val entryService: EntryService, private val referencePersister: ReferencePersister, private val tagService: TagService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        CommonComponent.component.inject(this)
    }


    fun saveEntryAsync(result: EntryExtractionResult, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveEntry(result))
        }
    }

    private fun saveEntry(result: EntryExtractionResult): Boolean {
        return saveEntry(result.entry, result.reference, result.tags)
    }


    fun saveEntryAsync(entry: Entry, reference: Reference? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveEntry(entry, reference, tags))
        }
    }

    private fun saveEntry(entry: Entry, reference: Reference? = null, tags: Collection<Tag> = ArrayList<Tag>()): Boolean {
        // by design at this stage there's no unpersisted tag -> set them directly on entry so that their ids get saved on entry with persist(entry) / update(entry)
        val removedTags = ArrayList(entry.tags)
        removedTags.removeAll(tags)

        entry.setAllTags(tags.filter { it != null })


        reference?.let {
            if(reference.isPersisted() == false) {
                referencePersister.saveReference(reference)
            }
        }

        val previousReference = entry.reference

        entry.reference = reference


        if(entry.isPersisted() == false) {
            entryService.persist(entry)
        }
        else {
            entryService.update(entry)
        }


        if(reference != previousReference) { // only update reference if it really changed
            reference?.let { referencePersister.saveReference(reference) }

            previousReference?.let { referencePersister.saveReference(it) }
        }


        tags.filterNotNull().forEach { tagService.update(it) } // TODO: check if tag needs an update

        removedTags.forEach { tagService.update(it) }


        return true
    }

}
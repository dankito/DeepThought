package net.dankito.deepthought.data

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.TagService
import net.dankito.utils.IThreadPool
import java.util.*
import javax.inject.Inject


class EntryPersister(private val entryService: EntryService, private val referencePersister: ReferencePersister, private val tagService: TagService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        BaseComponent.component.inject(this)
    }


    fun saveEntryAsync(result: ItemExtractionResult, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveEntry(result))
        }
    }

    private fun saveEntry(result: ItemExtractionResult): Boolean {
        return saveEntry(result.item, result.source, result.series, result.tags)
    }


    fun saveEntryAsync(item: Item, source: Source? = null, series: Series? = null, tags: Collection<Tag> = ArrayList(), callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveEntry(item, source, series, tags))
        }
    }

    private fun saveEntry(item: Item, source: Source? = null, series: Series? = null, tags: Collection<Tag> = ArrayList<Tag>()): Boolean {
        // by design at this stage there's no unpersisted tag -> set them directly on item so that their ids get saved on item with persist(item) / update(item)
        val removedTags = ArrayList(item.tags)
        removedTags.removeAll(tags)

        val addedTags = ArrayList(tags)
        addedTags.removeAll(item.tags)

        item.setAllTags(tags.filter { it != null })


        source?.let {
            if(source.isPersisted() == false) {
                referencePersister.saveReference(source, series)
            }
        }

        val previousReference = item.source

        item.source = source


        if(item.isPersisted() == false) {
            entryService.persist(item)
        }
        else {
            entryService.update(item)
        }


        if(source?.id != previousReference?.id) { // only update source if it really changed
            source?.let { referencePersister.saveReference(source, series, false) }

            previousReference?.let { referencePersister.saveReference(it, it.series, false) }
        }


        addedTags.filterNotNull().forEach { tagService.update(it) }

        removedTags.filterNotNull().forEach { tagService.update(it) }


        return true
    }

}
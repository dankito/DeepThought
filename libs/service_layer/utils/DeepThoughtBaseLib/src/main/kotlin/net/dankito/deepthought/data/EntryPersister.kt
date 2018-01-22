package net.dankito.deepthought.data

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.service.data.EntryService
import net.dankito.service.data.FileService
import net.dankito.service.data.TagService
import net.dankito.utils.IThreadPool
import java.util.*
import javax.inject.Inject


class EntryPersister(private val entryService: EntryService, private val referencePersister: ReferencePersister, private val tagService: TagService,
                     private val fileService: FileService) {

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
        return saveEntry(result.item, result.source, result.series, result.tags, result.files)
    }


    fun saveEntryAsync(item: Item, source: Source?, series: Series?, tags: Collection<Tag>, files: Collection<FileLink> = listOf(), callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveEntry(item, source, series, tags, files))
        }
    }

    private fun saveEntry(item: Item, source: Source? = null, series: Series? = null, tags: Collection<Tag> = ArrayList<Tag>(), files: Collection<FileLink>): Boolean {
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


        files.forEach { file ->
            if(file.isPersisted() == false) {
                fileService.persist(file)
            }
        }

        val removedFiles = ArrayList(item.attachedFiles)
        removedFiles.removeAll(files)

        val addedFiles = ArrayList(files)
        addedFiles.removeAll(item.attachedFiles)

        item.setAllAttachedFiles(files.filter { it != null })


        if(item.isPersisted() == false) {
            entryService.persist(item)
        }
        else {
            entryService.update(item)
        }


        if(source?.id != previousReference?.id) { // only update source if it really changed
            source?.let { referencePersister.saveReference(source, series, doChangesAffectDependentEntities = false) }

            previousReference?.let { referencePersister.saveReference(it, it.series, doChangesAffectDependentEntities = false) }
        }


        addedTags.filterNotNull().forEach { tagService.update(it) }

        removedTags.filterNotNull().forEach { tagService.update(it) }

        addedFiles.filterNotNull().forEach { fileService.update(it) }

        removedFiles.filterNotNull().forEach { fileService.update(it) }


        return true
    }

}
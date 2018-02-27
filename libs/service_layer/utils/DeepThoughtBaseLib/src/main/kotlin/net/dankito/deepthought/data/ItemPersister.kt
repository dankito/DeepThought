package net.dankito.deepthought.data

import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.model.*
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.service.data.DeleteEntityService
import net.dankito.service.data.ItemService
import net.dankito.service.data.TagService
import net.dankito.util.IThreadPool
import java.util.*
import javax.inject.Inject


class ItemPersister(private val itemService: ItemService, private val sourcePersister: SourcePersister, private val tagService: TagService,
                    private val filePersister: FilePersister, private val deleteEntityService: DeleteEntityService) {

    @Inject
    protected lateinit var threadPool: IThreadPool


    init {
        BaseComponent.component.inject(this)
    }


    fun saveItemAsync(result: ItemExtractionResult, callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveItem(result))
        }
    }

    private fun saveItem(result: ItemExtractionResult): Boolean {
        return saveItem(result.item, result.source, result.series, result.tags, result.files)
    }


    fun saveItemAsync(item: Item, source: Source?, series: Series?, tags: Collection<Tag>, files: Collection<DeepThoughtFileLink> = listOf(), callback: (Boolean) -> Unit) {
        threadPool.runAsync {
            callback(saveItem(item, source, series, tags, files))
        }
    }

    private fun saveItem(item: Item, source: Source? = null, series: Series? = null, tags: Collection<Tag> = ArrayList<Tag>(), files: Collection<DeepThoughtFileLink>): Boolean {
        val (addedTags, removedTags) = setTags(item, tags)

        val previousSource = setSource(item, source, series)

        val (addedFiles, removedFiles) = setFiles(item, files)


        if(item.isPersisted() == false) {
            itemService.persist(item)
        }
        else {
            itemService.update(item)
        }


        updateSource(source, previousSource, series)

        updateTagsAndFiles(addedTags, removedTags, addedFiles, removedFiles)


        return true
    }


    private fun setTags(item: Item, tags: Collection<Tag>): Pair<ArrayList<Tag>, ArrayList<Tag>> {
        // by design at this stage there's no unpersisted tag -> set them directly on item so that their ids get saved on item with persist(item) / update(item)
        val removedTags = ArrayList(item.tags)
        removedTags.removeAll(tags)

        val addedTags = ArrayList(tags)
        addedTags.removeAll(item.tags)

        item.setAllTags(tags.filter { it != null })

        return Pair(addedTags, removedTags)
    }

    private fun setSource(item: Item, source: Source?, series: Series?): Source? {
        source?.let {
            if(source.isPersisted() == false) {
                sourcePersister.saveSource(source, series)
            }
        }

        val previousSource = item.source

        item.source = source

        return previousSource
    }

    private fun setFiles(item: Item, files: Collection<DeepThoughtFileLink>): Pair<ArrayList<DeepThoughtFileLink>, ArrayList<DeepThoughtFileLink>> {
        ArrayList(files).forEach { file ->
            if(file.isPersisted() == false) {
                filePersister.saveFile(file)
            }
        }

        val removedFiles = ArrayList(item.attachedFiles)
        removedFiles.removeAll(files)

        val addedFiles = ArrayList(files)
        addedFiles.removeAll(item.attachedFiles)

        item.setAllAttachedFiles(files.filter { it != null })

        return Pair(addedFiles, removedFiles)
    }


    private fun updateSource(source: Source?, previousSource: Source?, series: Series?) {
        if(source?.id != previousSource?.id) { // only update source if it really changed
            source?.let { sourcePersister.saveSource(source, series, doChangesAffectDependentEntities = false) }

            previousSource?.let {
                sourcePersister.saveSource(previousSource, previousSource.series, doChangesAffectDependentEntities = false)
                deleteEntityService.mayDeleteSource(previousSource)
            }
        }
    }

    private fun updateTagsAndFiles(addedTags: ArrayList<Tag>, removedTags: ArrayList<Tag>, addedFiles: ArrayList<DeepThoughtFileLink>, removedFiles: ArrayList<DeepThoughtFileLink>) {
        addedTags.filterNotNull().forEach { tagService.update(it) }

        removedTags.filterNotNull().forEach { tagService.update(it) }

        addedFiles.filterNotNull().forEach { filePersister.saveFile(it) }

        removedFiles.filterNotNull().forEach { file ->
            filePersister.saveFile(file)
            deleteEntityService.mayDeleteFile(file)
        }
    }

}
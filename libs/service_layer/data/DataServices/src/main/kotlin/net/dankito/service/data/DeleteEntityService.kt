package net.dankito.service.data

import net.dankito.deepthought.model.*
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.model.ConfirmationDialogButton


/**
 * When simply calling <entityServiceBase>.delete() entity's sources aren't updated and still keep a source to deleted entity.
 * This service first removes all sources and updates them and then deletes the entity with its EntityService.
 */
class DeleteEntityService(private val entryService: EntryService, private val tagService: TagService, private val referenceService: ReferenceService, private val seriesService: SeriesService,
                          private val fileService: FileService, private val dialogService: IDialogService, private val threadPool: IThreadPool) {

    fun deleteEntryAsync(item: Item) {
        threadPool.runAsync { deleteEntry(item) }
    }

    fun deleteEntry(item: Item) {
        val entryReference = item.source
        item.source?.let { reference ->
            item.source = null
            referenceService.update(reference)
        }

        ArrayList(item.tags).filterNotNull().filter { it.id != null }.forEach { tag ->
            item.removeTag(tag)
            tagService.update(tag)
        }

        ArrayList(item.notes).filterNotNull().filter { it.id != null }.forEach { note ->
            item.removeNote(note)
            entryService.entityManager.updateEntity(note)
        }

        val attachedFiles = ArrayList(item.attachedFiles).filterNotNull().filter { it.id != null }
        attachedFiles.forEach { file ->
            item.removeAttachedFile(file)
            entryService.entityManager.updateEntity(file)
        }

        entryService.delete(item)

        mayAlsoDeleteReference(entryReference)
        mayAlsoDeleteFiles(attachedFiles)
    }

    private fun mayAlsoDeleteReference(source: Source?) {
        if(source?.hasItems() == false) { // this was the only Item on which Source has been set -> ask user if we should delete Source as well?
            val localizedMessage = dialogService.getLocalization().getLocalizedString("alert.message.item.was.only.item.on.source.delete.as.well", source.title)
            dialogService.showConfirmationDialog(localizedMessage) { selectedButton ->
                if(selectedButton == ConfirmationDialogButton.Confirm) {
                    deleteReference(source)
                }
            }
        }
    }

    private fun mayAlsoDeleteFiles(attachedFiles: List<FileLink>) {
        attachedFiles.forEach { file ->
            mayDeleteFile(file)
        }
    }

    fun mayDeleteFile(file: FileLink) {
        // TODO: may ask user first if file should be deleted?

        if(file.isAttachedToItems == false && file.isAttachedToSource == false) {
            deleteFile(file)
        }
    }


    fun deleteTagAsync(tag: Tag) {
        threadPool.runAsync { deleteTag(tag) }
    }

    fun deleteTag(tag: Tag) {
        ArrayList(tag.items).filterNotNull().filter { it.id != null }.forEach { entry ->
            entry.removeTag(tag)
            entryService.update(entry)
        }

        tagService.delete(tag)
    }


    fun deleteReferenceAsync(source: Source) {
        threadPool.runAsync { deleteReference(source) }
    }

    fun deleteReference(source: Source) {
        source.series?.let { series ->
            source.series = null
            seriesService.update(series)
        }

        ArrayList(source.items).filterNotNull().filter { it.id != null }.forEach { entry ->
            entry.source = null
            entryService.update(entry)
        }

        ArrayList(source.attachedFiles).filterNotNull().filter { it.id != null }.forEach { file ->
            source.removeAttachedFile(file)
            referenceService.entityManager.updateEntity(file)
        }

        referenceService.delete(source)
    }


    fun deleteSeriesAsync(series: Series) {
        threadPool.runAsync { deleteSeries(series) }
    }

    fun deleteSeries(series: Series) {
        ArrayList(series.sources).filterNotNull().filter { it.id != null }.forEach { reference ->
            reference.series = null
            referenceService.update(reference)
        }

        seriesService.delete(series)
    }


    fun deleteFileAsync(file: FileLink) {
        threadPool.runAsync { deleteFile(file) }
    }

    fun deleteFile(file: FileLink) {
        ArrayList(file.itemsAttachedTo).filterNotNull().filter { it.id != null }.forEach { item ->
            item.removeAttachedFile(file)
            entryService.update(item)
        }

        ArrayList(file.sourcesAttachedTo).filterNotNull().filter { it.id != null }.forEach { source ->
            source.removeAttachedFile(file)
            referenceService.update(source)
        }

        fileService.delete(file)
    }

}
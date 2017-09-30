package net.dankito.service.data

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.utils.IThreadPool
import net.dankito.utils.ui.IDialogService


/**
 * When simply calling <entityServiceBase>.delete() entity's references aren't updated and still keep a reference to deleted entity.
 * This service first removes all references and updates them and then deletes the entity with its EntityService.
 */
class DeleteEntityService(private val entryService: EntryService, private val tagService: TagService, private val referenceService: ReferenceService, private val seriesService: SeriesService,
                          private val dialogService: IDialogService, private val threadPool: IThreadPool) {

    fun deleteEntryAsync(entry: Entry) {
        threadPool.runAsync { deleteEntry(entry) }
    }

    fun deleteEntry(entry: Entry) {
        val entryReference = entry.reference
        entry.reference?.let { reference ->
            entry.reference = null
            referenceService.update(reference)
        }

        ArrayList(entry.tags).filterNotNull().filter { it.id != null }.forEach { tag ->
            entry.removeTag(tag)
            tagService.update(tag)
        }

        ArrayList(entry.notes).filterNotNull().filter { it.id != null }.forEach { note ->
            entry.removeNote(note)
            entryService.entityManager.updateEntity(note)
        }

        ArrayList(entry.attachedFiles).filterNotNull().filter { it.id != null }.forEach { file ->
            entry.removeAttachedFile(file)
            entryService.entityManager.updateEntity(file)
        }

        entryService.delete(entry)

        mayAlsoDeleteReference(entryReference)
    }

    private fun mayAlsoDeleteReference(reference: Reference?) {
        if (reference?.hasEntries() == false) { // this was the only Entry on which Reference has been set -> ask user if we should delete Reference as well?
            val localizedMessage = dialogService.getLocalization().getLocalizedString("alert.message.entry.was.only.entry.on.reference.delete.as.well", reference.title)
            dialogService.showConfirmationDialog(localizedMessage) { shouldAlsoDeleteReference ->
                if(shouldAlsoDeleteReference) {
                    deleteReference(reference)
                }
            }
        }
    }


    fun deleteTagAsync(tag: Tag) {
        threadPool.runAsync { deleteTag(tag) }
    }

    fun deleteTag(tag: Tag) {
        ArrayList(tag.entries).filterNotNull().filter { it.id != null }.forEach { entry ->
            entry.removeTag(tag)
            entryService.update(entry)
        }

        tagService.delete(tag)
    }


    fun deleteReferenceAsync(reference: Reference) {
        threadPool.runAsync { deleteReference(reference) }
    }

    fun deleteReference(reference: Reference) {
        reference.series?.let { series ->
            reference.series = null
            seriesService.update(series)
        }

        ArrayList(reference.entries).filterNotNull().filter { it.id != null }.forEach { entry ->
            entry.reference = null
            entryService.update(entry)
        }

        ArrayList(reference.attachedFiles).filterNotNull().filter { it.id != null }.forEach { file ->
            reference.removeAttachedFile(file)
            referenceService.entityManager.updateEntity(file)
        }

        referenceService.delete(reference)
    }


    fun deleteSeriesAsync(series: Series) {
        threadPool.runAsync { deleteSeries(series) }
    }

    fun deleteSeries(series: Series) {
        ArrayList(series.references).filterNotNull().filter { it.id != null }.forEach { reference ->
            reference.series = null
            referenceService.update(reference)
        }

        seriesService.delete(series)
    }

}
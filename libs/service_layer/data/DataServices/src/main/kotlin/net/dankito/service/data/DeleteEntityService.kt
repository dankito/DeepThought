package net.dankito.service.data

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.Tag
import net.dankito.utils.IThreadPool


/**
 * When simply calling <entityServiceBase>.delete() entity's references aren't updated and still keep a reference to deleted entity.
 * This service first removes all references and updates them and then deletes the entity with its EntityService.
 */
class DeleteEntityService(private val entryService: EntryService, private val tagService: TagService, private val referenceService: ReferenceService, private val threadPool: IThreadPool) {

    fun deleteEntryAsync(entry: Entry) {
        threadPool.runAsync { deleteEntry(entry) }
    }

    fun deleteEntry(entry: Entry) {
        entry.reference?.let { reference ->
            entry.reference = null
            referenceService.update(reference)
        }

        ArrayList(entry.tags).forEach { tag ->
            entry.removeTag(tag)
            tagService.update(tag)
        }

        ArrayList(entry.notes).forEach { note ->
            entry.removeNote(note)
            entryService.entityManager.updateEntity(note)
        }

        ArrayList(entry.embeddedFiles).forEach { file ->
            entry.removeEmbeddedFile(file)
            entryService.entityManager.updateEntity(file)
        }

        ArrayList(entry.attachedFiles).forEach { file ->
            entry.removeAttachedFile(file)
            entryService.entityManager.updateEntity(file)
        }

        entryService.delete(entry)
    }


    fun deleteTagAsync(tag: Tag) {
        threadPool.runAsync { deleteTag(tag) }
    }

    fun deleteTag(tag: Tag) {
        ArrayList(tag.entries).forEach { entry ->
            entry.removeTag(tag)
            entryService.update(entry)
        }

        tagService.delete(tag)
    }


    fun deleteReferenceAsync(reference: Reference) {
        deleteReference(reference)
    }

    fun deleteReference(reference: Reference) {
        ArrayList(reference.entries).forEach { entry ->
            entry.reference = null
            entryService.update(entry)
        }

        ArrayList(reference.embeddedFiles).forEach { file ->
            reference.removeEmbeddedFile(file)
            referenceService.entityManager.updateEntity(file)
        }

        ArrayList(reference.attachedFiles).forEach { file ->
            reference.removeAttachedFile(file)
            referenceService.entityManager.updateEntity(file)
        }

        referenceService.delete(reference)
    }

}
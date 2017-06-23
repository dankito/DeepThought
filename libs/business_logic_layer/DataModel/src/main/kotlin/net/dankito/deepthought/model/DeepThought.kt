package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.FileType
import net.dankito.deepthought.model.enums.NoteType
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.DeepThoughtTableName)
class DeepThought : UserDataEntity(), Serializable {

    companion object {
        private const val serialVersionUID = 441616313532856392L
    }


    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var entries: MutableList<Entry> = mutableListOf() // TODO: don't expose a mutable list
        private set

    @Column(name = TableConfig.DeepThoughtNextEntryIndexColumnName)
    var nextEntryIndex = 0L
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var tags: MutableList<Tag> = mutableListOf()
        private set


    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var references: MutableList<Reference> = mutableListOf()
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var notes: MutableList<Note> = mutableListOf()
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var files: MutableList<FileLink> = mutableListOf()
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var entriesGroups: MutableList<EntriesGroup> = mutableListOf()
        private set


    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy("createdOn DESC")
    var readLaterArticles: MutableList<ReadLaterArticle> = mutableListOf()
        private set


    @OneToMany(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy(value = "sortOrder")
    var noteTypes: MutableSet<NoteType> = TreeSet<NoteType>()
        private set

    @OneToMany(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy(value = "sortOrder")
    var fileTypes: MutableSet<FileType> = TreeSet<FileType>()
        private set


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.DeepThoughtDeepThoughtOwnerJoinColumnName)
    var deepThoughtOwner: User? = null
        internal set


    fun getCountEntries() : Int {
        return entries.size
    }

    fun addEntry(entry: Entry): Boolean {
        return entries.add(entry)
    }

    fun removeEntry(entry: Entry): Boolean {
        if (entries.remove(entry)) {
            removeAllRelationsFromEntry(entry)

            return true
        }

        return false
    }

    private fun removeAllRelationsFromEntry(entry: Entry) {
        for (tag in ArrayList(entry.tags)) {
            entry.removeTag(tag)
        }

        entry.reference = null

        for (note in ArrayList(entry.notes)) {
            entry.removeNote(note)
        }

        for (entriesGroup in ArrayList(entry.entryGroups)) {
            entry.removeEntriesGroup(entriesGroup)
        }

        for (attachedFile in ArrayList(entry.attachedFiles)) {
            entry.removeAttachedFile(attachedFile)
        }

        for (embeddedFile in ArrayList(entry.embeddedFiles)) {
            entry.removeEmbeddedFile(embeddedFile)
        }

        entry.previewImage = null
    }

    fun increaseNextEntryIndex(): Long {
        return ++nextEntryIndex
    }


    fun getCountTags() : Int {
        return tags.size
    }

    fun addTag(tag: Tag): Boolean {
        return tags.add(tag)
    }

    fun removeTag(tag: Tag): Boolean {
        if (tags.remove(tag)) {
            for (entry in ArrayList(tag.entries)) {
                entry.removeTag(tag)
            }

            return true
        }

        return false
    }


    fun getCountReferences() : Int {
        return references.size
    }

    fun addReference(reference: Reference): Boolean {
        return references.add(reference)
    }

    fun removeReference(reference: Reference): Boolean {
        if (references.remove(reference)) {
            for (entry in ArrayList(reference.entries)) {
                entry.reference = null
            }

            return true
        }

        return false
    }


    fun addNote(note: Note): Boolean {
        return notes.add(note)
    }

    fun removeNote(note: Note): Boolean {
        return notes.remove(note)
    }


    fun getCountFiles() : Int {
        return files.size
    }

    fun addFile(file: FileLink): Boolean {
        return files.add(file)
    }

    fun removeFile(file: FileLink): Boolean {
        if (files.remove(file)) {
            for (entry in ArrayList(file.entriesAttachedTo)) {
                entry.removeAttachedFile(file)
            }
            for (entry in ArrayList(file.entriesEmbeddedIn)) {
                entry.removeEmbeddedFile(file)
            }

            for (reference in ArrayList(file.referencesAttachedTo)) {
                reference.removeAttachedFile(file)
            }
            for (reference in ArrayList(file.referencesEmbeddedIn)) {
                reference.removeEmbeddedFile(file)
            }

            return true
        }

        return false
    }


    fun getCountEntriesGroups(): Int {
        return entriesGroups.size
    }

    fun addEntriesGroup(entriesGroup: EntriesGroup): Boolean {
        return entriesGroups.add(entriesGroup)
    }

    fun removeEntriesGroup(entriesGroup: EntriesGroup): Boolean {
        if (entriesGroups.remove(entriesGroup)) {
            for (entry in ArrayList(entriesGroup.entries)) {
                entry.removeEntriesGroup(entriesGroup)
            }

            return true
        }

        return false
    }


    fun getCountReadLaterArticles(): Int {
        return readLaterArticles.size
    }

    fun addReadLaterArticle(readLaterArticle: ReadLaterArticle): Boolean {
        return readLaterArticles.add(readLaterArticle)
    }

    fun removeEntriesGroup(readLaterArticle: ReadLaterArticle): Boolean {
        return readLaterArticles.remove(readLaterArticle)
    }

}

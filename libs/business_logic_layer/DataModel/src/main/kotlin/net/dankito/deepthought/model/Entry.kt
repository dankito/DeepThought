package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.io.Serializable
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList


@Entity(name = TableConfig.EntryTableName)
data class Entry(

        @Column(name = TableConfig.EntryContentColumnName)
        @Lob
        var content: String,

        @Column(name = TableConfig.EntryAbstractColumnName)
        @Lob
        var abstractString: String = "" // field cannot be named 'abstract' as this is a Java Keyword. So i named field abstractString but getter is called getAbstract()

) : BaseEntity(), Serializable {

    companion object {
        private const val serialVersionUID = 596730656893495215L
    }


    @Column(name = TableConfig.EntryEntryIndexColumnName)
    var entryIndex: Long = 0

    @ManyToMany(fetch = FetchType.LAZY)/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/
    @JoinTable(name = TableConfig.EntryTagJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.EntryTagJoinTableEntryIdColumnName)/*, referencedColumnName = "id"*/), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.EntryTagJoinTableTagIdColumnName)/*, referencedColumnName = "id"*/))
    //  @OrderBy("name ASC")
    var tags: MutableSet<Tag> = HashSet()
        private set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "entry", cascade = arrayOf(CascadeType.PERSIST))
    var notes: MutableSet<Note> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.EntryAttachedFilesJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.EntryAttachedFilesJoinTableEntryIdColumnName)/*, referencedColumnName = "id"*/), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.EntryAttachedFilesJoinTableFileLinkIdColumnName)/*, referencedColumnName = "id"*/))
    var attachedFiles: MutableSet<FileLink> = HashSet()
        private set

    @Column(name = TableConfig.EntryPreviewImageUrlColumnName)
    var previewImageUrl: String? = null

    @ManyToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.MERGE))
    @JoinColumn(name = TableConfig.EntryReferenceJoinColumnName)
    var reference: Reference? = null
        set(reference) {
            field?.removeEntry(this)

            field = reference

            reference?.addEntry(this)
        }

    @Column(name = TableConfig.EntryIndicationColumnName)
    var indication: String = ""


    private constructor() : this("")


    fun hasAbstract(): Boolean {
        return abstractString.isNotBlank()
    }

    fun hasContent(): Boolean {
        return content.isNotBlank()
    }


    fun hasTags(): Boolean {
        return countTags > 0
    }

    val countTags: Int
        get() = tags.size

    fun setAllTags(tags: Collection<Tag>) { // don't name it setTags(), would cause conflicts in Java (e.g. for deserializing with Jackson)
        for(previousTag in ArrayList(this.tags)) {
            removeTag(previousTag)
        }

        for(newTag in tags) {
            addTag(newTag)
        }
    }

    fun addTag(tag: Tag): Boolean {
        if (tags.add(tag)) {
            tag.addEntry(this)

            return true
        }

        return false
    }

    fun removeTag(tag: Tag): Boolean {
        if (tags.remove(tag)) {
            tag.removeEntry(this)

            return true
        }

        return false
    }


    fun hasReference(): Boolean {
        return reference != null
    }


    fun hasNotes(): Boolean {
        return notes.size > 0
    }

    fun addNote(note: Note): Boolean {
        if (notes.add(note)) {
            note.entry = this

            return true
        }

        return false
    }

    fun removeNote(note: Note): Boolean {
        if (notes.remove(note)) {
            note.entry = null

            return true
        }

        return false
    }


    fun hasAttachedFiles(): Boolean {
        return attachedFiles.size > 0
    }

    fun addAttachedFile(file: FileLink): Boolean {
        if (attachedFiles.contains(file)) {
            file.addAsAttachmentToEntry(this)

            return true
        }

        return false
    }

    fun removeAttachedFile(file: FileLink): Boolean {
        if (attachedFiles.remove(file)) {
            file.removeAsAttachmentFromEntry(this)

            return true
        }

        return false
    }


    fun hasIndication() : Boolean {
        return indication.isNotBlank()
    }


    override fun toString(): String {
        val contentLength = if(content.length > 100) 100 else content.length
        return "" + entryIndex + ", " + id + ": " + content.substring(0, contentLength)
    }

}

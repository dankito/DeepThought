package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.io.Serializable
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList


@Entity(name = TableConfig.ItemTableName)
data class Item(

        @Column(name = TableConfig.ItemContentColumnName)
        @Lob
        var content: String,

        @Column(name = TableConfig.ItemSummaryColumnName)
        var summary: String = ""

) : BaseEntity(), Serializable {

    companion object {
        private const val serialVersionUID = 596730656893495215L
    }


    @Column(name = TableConfig.ItemItemIndexColumnName)
    var itemIndex: Long = 0

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.ItemTagJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.ItemTagJoinTableItemIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ItemTagJoinTableTagIdColumnName)))
    var tags: MutableSet<Tag> = HashSet()
        private set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "item", cascade = arrayOf(CascadeType.PERSIST))
    var notes: MutableSet<Note> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.ItemAttachedFilesJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.ItemAttachedFilesJoinTableItemIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ItemAttachedFilesJoinTableFileLinkIdColumnName)))
    var attachedFiles: MutableSet<FileLink> = HashSet()
        private set

    @ManyToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.MERGE))
    @JoinColumn(name = TableConfig.ItemSourceJoinColumnName)
    var source: Source? = null
        set(source) {
            field?.removeItem(this)

            field = source

            source?.addItem(this)
        }

    @Column(name = TableConfig.ItemIndicationColumnName)
    var indication: String = ""

    @Column(name = TableConfig.ItemPreviewColumnName)
    var preview: String = ""


    private constructor() : this("")


    fun hasAbstract(): Boolean {
        return summary.isNotBlank()
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
        val copy = ArrayList(tags)  // make a copy. if tags equal this.tags, all tags would get removed by removeTag() otherwise

        for(previousTag in ArrayList(this.tags)) {
            removeTag(previousTag)
        }

        for(newTag in copy) {
            addTag(newTag)
        }
    }

    fun addTag(tag: Tag): Boolean {
        if (tags.add(tag)) {
            tag.addItem(this)

            return true
        }

        return false
    }

    fun removeTag(tag: Tag): Boolean {
        if (tags.remove(tag)) {
            tag.removeItem(this)

            return true
        }

        return false
    }


    fun hasSource(): Boolean {
        return source != null
    }


    fun hasNotes(): Boolean {
        return notes.size > 0
    }

    fun addNote(note: Note): Boolean {
        if (notes.add(note)) {
            note.item = this

            return true
        }

        return false
    }

    fun removeNote(note: Note): Boolean {
        if (notes.remove(note)) {
            note.item = null

            return true
        }

        return false
    }


    fun hasAttachedFiles(): Boolean {
        return attachedFiles.size > 0
    }

    fun setAllAttachedFiles(files: Collection<FileLink>) { // don't name it setAttachedFiles(), would cause conflicts in Java (e.g. for deserializing with Jackson)
        val copy = ArrayList(files)  // make a copy. if files equals this.attachedFiles, all files would get removed by removeAttachedFile() otherwise

        for(previousFile in ArrayList(this.attachedFiles)) {
            removeAttachedFile(previousFile)
        }

        for(newFile in copy) {
            addAttachedFile(newFile)
        }
    }

    fun addAttachedFile(file: FileLink): Boolean {
        if(attachedFiles.add(file)) {
            file.addAsAttachmentToItem(this)

            return true
        }

        return false
    }

    fun removeAttachedFile(file: FileLink): Boolean {
        if(attachedFiles.remove(file)) {
            file.removeAsAttachmentFromItem(this)

            return true
        }

        return false
    }


    fun hasIndication() : Boolean {
        return indication.isNotBlank()
    }


    override fun toString(): String {
        val contentLength = if(content.length > 100) 100 else content.length
        return "" + itemIndex + ", " + id + ": " + content.substring(0, contentLength) + "; " + source
    }

}

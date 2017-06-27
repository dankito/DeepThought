package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.FileType
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.FileLinkTableName)
data class FileLink(

        @Column(name = TableConfig.FileLinkUriColumnName)
        var uriString: String,

        @Column(name = TableConfig.FileLinkNameColumnName)
        var name: String = "",

        @Column(name = TableConfig.FileLinkIsFolderColumnName)
        var isFolder: Boolean = false

) : BaseEntity(), Serializable {

    // TODO: automatically set name and fileType when uriString gets set


    companion object {
        private const val serialVersionUID = -7508656557829870722L
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.FileLinkFileTypeColumnName)
    var fileType: FileType? = null

    @Column(name = TableConfig.FileLinkDescriptionColumnName)
    var description = ""

    @Column(name = TableConfig.FileLinkSourceUriColumnName)
    var sourceUriString = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
    var entriesAttachedTo: MutableSet<Entry> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "embeddedFiles")
    var entriesEmbeddedIn: MutableSet<Entry> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
    var referencesAttachedTo: MutableSet<Reference> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "embeddedFiles")
    var referencesEmbeddedIn: MutableSet<Reference> = HashSet()
        private set


    init {
        // TODO
//        this.fileType = FileType.getDefaultFileType()
    }


    private constructor() : this("")


    val isAttachedToEntries: Boolean
        get() = entriesAttachedTo.size > 0

    internal fun addAsAttachmentToEntry(entry: Entry): Boolean {
        return entriesAttachedTo.add(entry)
    }

    internal fun removeAsAttachmentFromEntry(entry: Entry): Boolean {
        return entriesAttachedTo.remove(entry)
    }

    val isEmbeddedInEntries: Boolean
        get() = entriesEmbeddedIn.size > 0

    internal fun addAsEmbeddingToEntry(entry: Entry): Boolean {
        return entriesEmbeddedIn.add(entry)
    }

    internal fun removeAsEmbeddingFromEntry(entry: Entry): Boolean {
        return entriesEmbeddedIn.remove(entry)
    }


    val isAttachedToReferences: Boolean
        get() = referencesAttachedTo.size > 0

    internal fun addAsAttachmentToReference(reference: Reference): Boolean {
        return referencesAttachedTo.add(reference)
    }

    internal fun removeAsAttachmentFromReference(reference: Reference): Boolean {
        return referencesAttachedTo.remove(reference)
    }

    val isEmbeddedInReferences: Boolean
        get() = referencesEmbeddedIn.size > 0

    internal fun addAsEmbeddingToReference(reference: Reference): Boolean {
        return referencesEmbeddedIn.add(reference)
    }

    internal fun removeAsEmbeddingFromReference(reference: Reference): Boolean {
        return referencesEmbeddedIn.remove(reference)
    }

}

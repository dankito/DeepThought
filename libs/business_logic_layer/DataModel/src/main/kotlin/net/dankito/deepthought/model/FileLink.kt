package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.FileType
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.FileLinkTableName)
data class FileLink(

        /**
         * Absolute uri for remote file, relative path for local files
         */
        @Column(name = TableConfig.FileLinkUriColumnName)
        var uriString: String,

        @Column(name = TableConfig.FileLinkNameColumnName)
        var name: String = "",

        @Column(name = TableConfig.FileLinkIsLocalFileColumnName)
        var isLocalFile: Boolean = false

) : BaseEntity(), Serializable {

    // TODO: automatically set name and fileType when uriString gets set


    companion object {
        const val SizeNotDeterminedYet = -1L

        private const val serialVersionUID = -7508656557829870722L
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.FileLinkFileTypeColumnName)
    var fileType: FileType? = null

    @Column(name = TableConfig.FileLinkFileSizeColumnName)
    var fileSize: Long = SizeNotDeterminedYet

    @Column(name = TableConfig.FileLinkFileLastModifiedColumnName)
    var fileLastModified: Date? = null

    @Column(name = TableConfig.FileLinkFileHashSHA512ColumnName)
    var hashSHA512 = ""

    @Column(name = TableConfig.FileLinkDescriptionColumnName)
    var description = ""

    @Column(name = TableConfig.FileLinkSourceUriColumnName)
    var sourceUriString = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
    var itemsAttachedTo: MutableSet<Item> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
    var sourcesAttachedTo: MutableSet<Source> = HashSet()
        private set


    @Transient
    @kotlin.jvm.Transient
    var localFileInfo: LocalFileInfo? = null


    init {
        // TODO
//        this.fileType = FileType.getDefaultFileType()
    }


    internal constructor() : this("")


    val isAttachedToItems: Boolean
        get() = itemsAttachedTo.size > 0

    internal fun addAsAttachmentToItem(item: Item): Boolean {
        return itemsAttachedTo.add(item)
    }

    internal fun removeAsAttachmentFromItem(item: Item): Boolean {
        return itemsAttachedTo.remove(item)
    }


    val isAttachedToSource: Boolean
        get() = sourcesAttachedTo.size > 0

    internal fun addAsAttachmentToSource(source: Source): Boolean {
        return sourcesAttachedTo.add(source)
    }

    internal fun removeAsAttachmentFromSource(source: Source): Boolean {
        return sourcesAttachedTo.remove(source)
    }

}

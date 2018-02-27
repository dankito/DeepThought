package net.dankito.synchronization.model

import net.dankito.synchronization.model.config.TableConfig
import net.dankito.synchronization.model.enums.FileType
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated


@Entity(name = TableConfig.FileLinkTableName)
open class FileLink(

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


    @Column(name = TableConfig.FileLinkMimeTypeColumnName)
    var mimeType: String? = null

    @Enumerated(EnumType.ORDINAL)
    @Column(name = TableConfig.FileLinkFileTypeColumnName)
    var fileType: FileType = FileType.Other

    @Column(name = TableConfig.FileLinkFileSizeColumnName)
    var fileSize: Long = SizeNotDeterminedYet

    @Column(name = TableConfig.FileLinkFileLastModifiedColumnName)
    var fileLastModified: Date? = null

    @Column(name = TableConfig.FileLinkFileHashSHA256ColumnName)
    var hashSHA256 = ""

    @Column(name = TableConfig.FileLinkDescriptionColumnName)
    var description = ""

    @Column(name = TableConfig.FileLinkSourceUriColumnName)
    var sourceUriString = ""


    @kotlin.jvm.Transient
    @javax.persistence.Transient
    private val uuid = UUID.randomUUID().toString() // just needed for same internal states, as for instance equality only uriString, name and isLocalFile are taken into
    // consideration - so two instances having the same values in these are considered equal. And as FileLinks may remain unpersisted for some time, we also cannot take id field


    internal constructor() : this("")


    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is FileLink) return false

        return uuid == other.uuid
    }

    override fun hashCode(): Int {
        var result = (uuid ?: "").hashCode()

        return result
    }

}

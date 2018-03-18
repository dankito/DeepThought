package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.FileSyncStatus
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.LocalFileInfoTableName)
data class LocalFileInfo(

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = TableConfig.LocalFileInfoFileLinkJoinColumnName)
        val file: FileLink,

        @Column(name = TableConfig.LocalFileInfoPathColumnName)
        var path: String? = null,

        @Column(name = TableConfig.LocalFileInfoIsDeviceThatHasOriginalColumnName)
        var isDeviceThatHasOriginal: Boolean = false,

        @Enumerated(EnumType.ORDINAL)
        @Column(name = TableConfig.LocalFileInfoSyncStatusColumnName)
        var syncStatus: FileSyncStatus = FileSyncStatus.NotSynchronizedYet,


        @Column(name = TableConfig.LocalFileInfoFileSizeColumnName)
        var fileSize: Long = FileLink.SizeNotDeterminedYet,

        @Column(name = TableConfig.LocalFileInfoFileLastModifiedColumnName)
        var fileLastModified: Date? = null,

        @Column(name = TableConfig.LocalFileInfoFileHashSHA256ColumnName)
        var hashSHA256: String = ""


) : BaseEntity(), Serializable {

    companion object {
        private const val serialVersionUID = -7176297657016698447L
    }


    private constructor() : this(FileLink())


}
package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList


@Entity(name = TableConfig.ReferenceTableName)
data class Reference(

        @Column(name = TableConfig.ReferenceTitleColumnName)
        var title: String,

        @Column(name = TableConfig.ReferenceSubTitleColumnName)
        var subTitle: String = ""

) : BaseEntity() {

    companion object {
        private const val serialVersionUID = -7176298227016698447L
    }


    @Column(name = TableConfig.ReferenceUrlColumnName)
    var url: String? = null

    @Column(name = TableConfig.ReferenceSeriesColumnName)
    var series: String? = null

    @Column(name = TableConfig.ReferenceIssueColumnName)
    var issue: String? = null

    @Column(name = TableConfig.ReferencePublishingDateColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var publishingDate: Date? = null


    @Column(name = TableConfig.ReferenceAbstractColumnName)
    var abstractString: String? = null

    @Column(name = TableConfig.ReferenceLengthColumnName)
    var length: String? = null

    @Column(name = TableConfig.ReferenceLastAccessDateColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var lastAccessDate: Date? = null


    @Column(name = TableConfig.ReferenceTableOfContentsColumnName)
    @Lob
    var tableOfContents: String? = null

    @Column(name = TableConfig.ReferenceIsbnOrIssnColumnName)
    var isbnOrIssn: String? = null

    @Column(name = TableConfig.ReferenceNotesColumnName)
    var notes: String? = null



    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference")
    var entries: MutableList<Entry> = ArrayList() // TODO: don't expose a mutable list to the outside
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.ReferenceBaseAttachedFileJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseAttachedFileJoinTableReferenceBaseIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseAttachedFileJoinTableFileLinkIdColumnName)))
    var attachedFiles: MutableList<FileLink> = ArrayList()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableReferenceBaseIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableFileLinkIdColumnName)))
    var embeddedFiles: MutableList<FileLink> = ArrayList()
        private set

    @OneToOne(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @JoinColumn(name = TableConfig.ReferencePreviewImageJoinColumnName)
    var previewImage: FileLink? = null


    private constructor() : this("")

    constructor(url: String, title: String, publishingDate: Date? = null, series: String? = null, subTitle: String = "") : this(title, subTitle) {
        this.url = url
        this.publishingDate = publishingDate
        this.series = series
    }


    fun hasEntries(): Boolean {
        return entries.size > 0
    }

    internal fun addEntry(entry: Entry): Boolean {
        return entries.add(entry)
    }

    internal fun removeEntry(entry: Entry): Boolean {
        return entries.remove(entry)
    }


    fun hasAttachedFiles(): Boolean {
        return attachedFiles.size > 0
    }

    fun addAttachedFile(file: FileLink): Boolean {
        if (attachedFiles.add(file)) {
            file.addAsAttachmentToReference(this)

            return true
        }

        return false
    }

    fun removeAttachedFile(file: FileLink): Boolean {
        if (attachedFiles.remove(file)) {
            file.removeAsAttachmentFromReference(this)

            return true
        }

        return false
    }


    fun hasEmbeddedFiles(): Boolean {
        return embeddedFiles.size > 0
    }

    fun addEmbeddedFile(file: FileLink): Boolean {
        if (embeddedFiles.add(file)) {
            file.addAsEmbeddingToReference(this)

            return true
        }

        return false
    }

    fun removeEmbeddedFile(file: FileLink): Boolean {
        if (embeddedFiles.remove(file)) {
            file.removeAsEmbeddingFromReference(this)

            return true
        }

        return false
    }


    fun hasPreviewImage(): Boolean {
        return previewImage != null
    }

}

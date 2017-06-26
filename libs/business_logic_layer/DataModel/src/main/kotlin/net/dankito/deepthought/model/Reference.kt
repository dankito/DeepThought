package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.ReferenceTableName)
data class Reference(

        @Column(name = TableConfig.ReferenceTitleColumnName)
        var title: String,

        @Column(name = TableConfig.ReferenceSubTitleColumnName)
        var subTitle: String = ""

) : UserDataEntity() {

    // TODO: reset preview on changes to title, publishingDate or issueOrPublishingDate
    // TODO: reproduces setting publishingDate with tryToParseIssueOrPublishingDateToDate() when issueOrPublishingDate gets set


    companion object {
        private const val serialVersionUID = -7176298227016698447L
    }


    @Column(name = TableConfig.ReferenceAbstractColumnName)
    var abstractString: String? = null

    @Column(name = TableConfig.ReferenceLengthColumnName)
    var length: String? = null

    @Column(name = TableConfig.ReferenceUrlColumnName)
    var url: String? = null

    @Column(name = TableConfig.ReferenceSeriesColumnName)
    var series: String? = null

    @Column(name = TableConfig.ReferenceLastAccessDateColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var lastAccessDate: Date? = null

    @Column(name = TableConfig.ReferenceNotesColumnName)
    var notes: String? = null


    @Column(name = TableConfig.ReferenceTableOfContentsColumnName)
    @Lob
    var tableOfContents: String? = null

    @Column(name = TableConfig.ReferenceIssueOrPublishingDateColumnName)
    var issueOrPublishingDate: String? = null

    @Column(name = TableConfig.ReferenceIsbnOrIssnColumnName)
    var isbnOrIssn: String? = null

    @Column(name = TableConfig.ReferencePublishingDateColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var publishingDate: Date? = null



    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference")
    var entries: MutableSet<Entry> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.ReferenceBaseAttachedFileJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseAttachedFileJoinTableReferenceBaseIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseAttachedFileJoinTableFileLinkIdColumnName)))
    var attachedFiles: MutableSet<FileLink> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableReferenceBaseIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableFileLinkIdColumnName)))
    var embeddedFiles: MutableSet<FileLink> = HashSet()
        private set

    @OneToOne(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @JoinColumn(name = TableConfig.ReferencePreviewImageJoinColumnName)
    var previewImage: FileLink? = null


    private constructor() : this("")

    constructor(url: String, title: String, publishingDate: Date? = null) : this(title, "") {
        this.url = url
        this.publishingDate = publishingDate
    }

    constructor(url: String, title: String, publishingDate: Date? = null, series: String? = null) : this(url, title, publishingDate) {
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

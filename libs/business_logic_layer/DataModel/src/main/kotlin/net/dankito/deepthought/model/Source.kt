package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.synchronization.model.BaseEntity
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList


@Entity(name = TableConfig.SourceTableName)
data class Source(

        @Column(name = TableConfig.SourceTitleColumnName)
        var title: String

) : BaseEntity() {

    companion object {
        private const val serialVersionUID = -7176298227016698447L
    }


    @Column(name = TableConfig.SourceSubTitleColumnName)
    var subTitle: String = ""

    @Column(name = TableConfig.SourceUrlColumnName)
    var url: String? = null

    @ManyToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.MERGE))
    @JoinColumn(name = TableConfig.SourceSeriesJoinColumnName)
    var series: Series? = null
        set(series) {
            field?.removeSource(this)

            field = series

            series?.addSource(this)
        }

    @Column(name = TableConfig.SourceIssueColumnName)
    var issue: String? = null

    @Column(name = TableConfig.SourcePublishingDateColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var publishingDate: Date? = null

    @Column(name = TableConfig.SourcePublishingDateStringColumnName)
    var publishingDateString: String? = null
        private set


    @Column(name = TableConfig.SourceAbstractColumnName)
    var abstractString: String? = null

    @Column(name = TableConfig.SourceLengthColumnName)
    var length: String? = null

    @Column(name = TableConfig.SourceLastAccessDateColumnName)
    @Temporal(TemporalType.TIMESTAMP)
    var lastAccessDate: Date? = null


    @Column(name = TableConfig.SourceTableOfContentsColumnName)
    @Lob // TODO: evaluate if we should save it as LOB or not
    var tableOfContents: String? = null

    @Column(name = TableConfig.SourceIsbnOrIssnColumnName)
    var isbnOrIssn: String? = null

    @Column(name = TableConfig.SourceNotesColumnName)
    var notes: String? = null



    @OneToMany(fetch = FetchType.LAZY, mappedBy = "source")
    var items: MutableList<Item> = ArrayList() // TODO: don't expose a mutable list to the outside
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.SourceAttachedFileJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.SourceAttachedFileJoinTableSourceBaseIdColumnName)), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.SourceAttachedFileJoinTableFileLinkIdColumnName)))
    var attachedFiles: MutableList<FileLink> = ArrayList()
        private set

    @Column(name = TableConfig.SourcePreviewImageUrlColumnName)
    var previewImageUrl: String? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @JoinColumn(name = TableConfig.SourcePreviewImageJoinColumnName)
    var previewImage: FileLink? = null


    constructor() : this("")

    constructor(title: String, url: String, publishingDate: Date? = null, previewImageUrl: String? = null, subTitle: String = "", series: Series? = null) : this(title) {
        this.url = url
        this.publishingDate = publishingDate
        this.previewImageUrl = previewImageUrl
        this.subTitle = subTitle
        this.series = series
    }


    fun setPublishingDate(publishingDate: Date?, publishingDateString: String? = null) {
        this.publishingDate = publishingDate
        this.publishingDateString = publishingDateString
    }


    fun hasItems(): Boolean {
        return items.size > 0
    }

    internal fun addItem(item: Item): Boolean {
        return items.add(item)
    }

    internal fun removeItem(item: Item): Boolean {
        return items.remove(item)
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
            file.addAsAttachmentToSource(this)

            return true
        }

        return false
    }

    fun removeAttachedFile(file: FileLink): Boolean {
        if(attachedFiles.remove(file)) {
            file.removeAsAttachmentFromSource(this)

            return true
        }

        return false
    }


    fun hasPreviewImage(): Boolean {
        return previewImage != null
    }


    override fun toString(): String {
        var description = title

        if(subTitle.isNullOrBlank() == false) {
            description = description + " - " + subTitle
        }

        if(url.isNullOrBlank() == false) {
            description = description + "; " + url
        }

        if(issue.isNullOrBlank() == false || publishingDate != null) {
            description = issue + " " + publishingDate + ": " + description
        }

        if(series != null) {
            description = "" + series + " " + description
        }

        return description
    }

}

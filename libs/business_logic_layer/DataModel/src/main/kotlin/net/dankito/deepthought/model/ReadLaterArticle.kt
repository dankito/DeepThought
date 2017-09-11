package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.util.EntryExtractionResult
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob


@Entity(name = TableConfig.ReadLaterArticleTableName)
class ReadLaterArticle(

        @Transient
        var entryExtractionResult: EntryExtractionResult, // do not try to persist EntryExtractionResult as this would persist an unpersisted entry (and may reference)

        @Column(name = TableConfig.ReadLaterArticleEntryPreviewColumnName)
        var entryPreview: String = "",

        @Column(name = TableConfig.ReadLaterArticleReferencePreviewColumnName)
        var referencePreview: String = "",

        @Column(name = TableConfig.ReadLaterArticleReferenceUrlColumnName)
        var referenceUrl: String? = null,

        @Column(name = TableConfig.ReadLaterArticlePreviewImageUrlColumnName)
        var previewImageUrl: String? = null

) : BaseEntity() {

    companion object {
        private const val serialVersionUID = 1204202485407318616L
    }


    private constructor() : this(EntryExtractionResult(Entry("")))


    @Column(name = TableConfig.ReadLaterArticleEntryExtractionResultColumnName)
    @Lob
    var serializedEntryExtractionResult: String = ""

}
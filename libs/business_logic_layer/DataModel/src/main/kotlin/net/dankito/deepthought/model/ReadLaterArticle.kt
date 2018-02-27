package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.synchronization.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob


@Entity(name = TableConfig.ReadLaterArticleTableName)
class ReadLaterArticle(

        @Transient
        var itemExtractionResult: ItemExtractionResult, // do not try to persist ItemExtractionResult as this would persist an unpersisted item (and may source)

        @Column(name = TableConfig.ReadLaterArticleItemPreviewColumnName)
        var itemPreview: String = "",

        @Column(name = TableConfig.ReadLaterArticleSourcePreviewColumnName)
        var sourcePreview: String = "",

        @Column(name = TableConfig.ReadLaterArticleSourceUrlColumnName)
        var sourceUrl: String? = null,

        @Column(name = TableConfig.ReadLaterArticlePreviewImageUrlColumnName)
        var previewImageUrl: String? = null

) : BaseEntity() {

    companion object {
        private const val serialVersionUID = 1204202485407318616L
    }


    private constructor() : this(ItemExtractionResult(Item("")))


    @Column(name = TableConfig.ReadLaterArticleItemExtractionResultColumnName)
    @Lob
    var serializedItemExtractionResult: String = ""

}
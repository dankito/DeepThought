package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.util.EntryExtractionResult
import javax.persistence.Column
import javax.persistence.Entity


@Entity(name = TableConfig.ReadLaterArticleTableName)
class ReadLaterArticle(

        @Transient
        var entryExtractionResult: EntryExtractionResult // do not try to persist EntryExtractionResult as this would persist an unpersisted entry (and may reference)

) : BaseEntity() {

    companion object {
        private const val serialVersionUID = 1204202485407318616L
    }


    private constructor() : this(EntryExtractionResult(Entry("")))


    @Column(name = TableConfig.ReadLaterArticleEntryExtractionResultColumnName)
    var serializedEntryExtractionResult: String = ""

}
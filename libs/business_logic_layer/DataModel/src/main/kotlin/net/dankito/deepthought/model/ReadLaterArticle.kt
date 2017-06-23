package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.Column
import javax.persistence.Entity


@Entity(name = TableConfig.ReadLaterArticleTableName)
class ReadLaterArticle(

        @Column(name = TableConfig.ReadLaterArticleEntryExtractionResultColumnName)
        var serializedEntryExtractionResult: String

) : UserDataEntity() {

    companion object {
        private const val serialVersionUID = 1204202485407318616L
    }


    private constructor() : this("")

}
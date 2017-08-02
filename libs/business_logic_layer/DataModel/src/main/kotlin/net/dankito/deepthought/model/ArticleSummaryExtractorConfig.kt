package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.Column
import javax.persistence.Entity


@Entity(name = TableConfig.ArticleSummaryExtractorConfigTableName)
data class ArticleSummaryExtractorConfig(

        @Column(name = TableConfig.ArticleSummaryExtractorConfigUrlColumnName)
        val url: String,

        @Column(name = TableConfig.ArticleSummaryExtractorConfigNameColumnName)
        var name: String,

        @Column(name = TableConfig.ArticleSummaryExtractorConfigIconUrlColumnName)
        var iconUrl: String? = null,

        @Column(name = TableConfig.ArticleSummaryExtractorConfigSortOrderColumnName)
        var sortOrder: Int = Int.MAX_VALUE,

        /**
         * For RSS and Atom Feed Extractors url is the feedUrl, siteUrl then holds the url to the according site
         */
        @Column(name = TableConfig.ArticleSummaryExtractorConfigSiteUrlColumnName)
        val siteUrl: String? = null,

        @Column(name = TableConfig.ArticleSummaryExtractorConfigIsFavoriteColumnName)
        var isFavorite: Boolean = false,

        @Column(name = TableConfig.ArticleSummaryExtractorConfigFavoriteIndexColumnName)
        var favoriteIndex: Int? = null

) :BaseEntity() {

    private constructor() : this("", "")

}
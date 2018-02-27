package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.synchronization.model.BaseEntity
import java.util.*
import javax.persistence.*


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

) : BaseEntity() {

    private constructor() : this("", "")


        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = TableConfig.ArticleSummaryExtractorConfigTagsToAddJoinTableName,
                joinColumns = arrayOf(JoinColumn(name = TableConfig.ArticleSummaryExtractorConfigTagsToAddJoinTableArticleSummaryExtractorConfigIdColumnName)),
                inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.ArticleSummaryExtractorConfigTagsToAddJoinTableTagIdColumnName)))
        var tagsToAddOnExtractedArticles: MutableSet<Tag> = HashSet() // TODO: don't expose a mutable set to the outside
                private set


        fun addTagToAddOnExtractedArticles(tag: Tag): Boolean {
            return tagsToAddOnExtractedArticles.add(tag)
        }

        fun removeTagToAddOnExtractedArticles(tag: Tag): Boolean {
            return tagsToAddOnExtractedArticles.remove(tag)
        }

}
package net.dankito.deepthought.news.summary.config

import net.dankito.newsreader.summary.IArticleSummaryExtractor


data class ArticleSummaryExtractorConfig(@Transient var extractor: IArticleSummaryExtractor?, val url: String, var name: String, var iconUrl: String? = null,
                                         var isFavorite: Boolean = false, var favoriteIndex: Int? = null) {

    private constructor() : this(null, "", "") // for Jackson

}
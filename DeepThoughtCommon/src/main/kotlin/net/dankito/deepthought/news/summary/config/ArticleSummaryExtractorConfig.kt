package net.dankito.deepthought.news.summary.config

import net.dankito.newsreader.summary.IArticleSummaryExtractor


data class ArticleSummaryExtractorConfig(@Transient var extractor: IArticleSummaryExtractor?, val url: String, val name: String, var iconUrl: String? = null) {

    private constructor() : this(null, "", "") // for Jackson

}
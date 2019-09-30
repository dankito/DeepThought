package net.dankito.deepthought.ui.windowdata

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.ArticleSummary
import net.dankito.newsreader.model.ArticleSummaryItem


data class ArticleSummaryWindowData(val extractorConfig: ArticleSummaryExtractorConfig,
                                    var summary: ArticleSummary? = null,
                                    var selectedItems: List<ArticleSummaryItem> = listOf()) {

    private constructor() : this(ArticleSummaryExtractorConfig("", "")) // for Jackson

}
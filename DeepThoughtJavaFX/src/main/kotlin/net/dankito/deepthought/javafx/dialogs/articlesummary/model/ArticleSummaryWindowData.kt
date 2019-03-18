package net.dankito.deepthought.javafx.dialogs.articlesummary.model

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.ArticleSummary


data class ArticleSummaryWindowData(val articleSummaryExtractorConfig: ArticleSummaryExtractorConfig,
                                    var articleSummary: ArticleSummary? = null) {

    private constructor() : this(ArticleSummaryExtractorConfig("", "")) // for Jackson

}
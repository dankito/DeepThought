package net.dankito.deepthought.android.activities.arguments

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.newsreader.model.ArticleSummary


data class ArticleSummaryActivityParameters(val extractorConfig: ArticleSummaryExtractorConfig, val summary: ArticleSummary? = null)
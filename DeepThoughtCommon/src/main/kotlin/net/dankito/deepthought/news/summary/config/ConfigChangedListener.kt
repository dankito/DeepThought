package net.dankito.deepthought.news.summary.config

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig


interface ConfigChangedListener {

    fun configChanged(config: ArticleSummaryExtractorConfig)

}
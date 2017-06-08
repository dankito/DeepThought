package net.dankito.deepthought.news.summary.config


interface ConfigChangedListener {

    fun configChanged(config: ArticleSummaryExtractorConfig)

}
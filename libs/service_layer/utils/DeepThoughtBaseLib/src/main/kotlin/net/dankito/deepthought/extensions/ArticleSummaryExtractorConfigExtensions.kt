package net.dankito.deepthought.extensions

import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.newsreader.summary.IArticleSummaryExtractor
import java.util.concurrent.ConcurrentHashMap


private val summaryExtractorsCache = ConcurrentHashMap<String, IArticleSummaryExtractor>()


var ArticleSummaryExtractorConfig.extractor: IArticleSummaryExtractor?
    get() {
        return summaryExtractorsCache[this.url]
    }
    set(value) {
        if(value != null) {
            summaryExtractorsCache.put(this.url, value)
        }
        else {
            summaryExtractorsCache.remove(this.url)
        }
    }

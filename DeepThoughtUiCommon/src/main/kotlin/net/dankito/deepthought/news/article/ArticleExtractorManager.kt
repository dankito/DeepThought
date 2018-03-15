package net.dankito.deepthought.news.article

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.SeriesService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.SeriesSearch
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


open class ArticleExtractorManager(private val seriesService: SeriesService, private val searchEngine: ISearchEngine) {

    companion object {
        private val log = LoggerFactory.getLogger(ArticleExtractorManager::class.java)
    }


    @Inject
    protected lateinit var articleExtractors: ArticleExtractors


    init {
        CommonComponent.component.inject(this)
    }


    fun extractArticleUserDidSeeBeforeAndAddDefaultDataAsync(item: ArticleSummaryItem, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            log.info("Using $extractor to extract item ${item.title}")

            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let { extractedItem(extractor, item, it, asyncResult, callback) }
                asyncResult.error?.let { callback(asyncResult) }
            }
        }
    }

    private fun extractedItem(extractor: IArticleExtractor, item: ArticleSummaryItem, extractionResult: ItemExtractionResult, originalAsyncResult: AsyncResult<ItemExtractionResult>,
                              callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        if(extractionResult.couldExtractContent || articleExtractors.isDefaultExtractor(extractor)) {
            addDefaultData(extractor, item, extractionResult) { callback(originalAsyncResult) }
        }
        else { // couldExtractContent == false and extractor != defaultExtractor -> try with default extractor
            articleExtractors.defaultExtractor.extractArticleAsync(item) { newAsyncResult ->
                newAsyncResult.result?.let { addDefaultData(extractor, item, it) { callback(newAsyncResult) } }
                newAsyncResult.error?.let { addDefaultData(extractor, item, extractionResult) { callback(originalAsyncResult) } } // ok, then use original result
            }
        }
    }

    /**
     * Do to legal issues, don't extract an article from a url the user didn't for sure see before. Show her/him original web page before
     */
    open fun extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(item: ArticleSummaryItem, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        // TODO: what to do with summary then?
        val extractionResult = ItemExtractionResult(Item(""), Source(item.title, item.url, item.publishedDate, item.previewImageUrl))

        addDefaultData(item, extractionResult) {
            callback(AsyncResult(true, result = extractionResult))
        }
    }

    fun extractArticleUserDidSeeBeforeAndAddDefaultDataAsync(url: String, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        articleExtractors.getExtractorForUrl(url)?.let { extractor ->
            log.info("Using $extractor to extract url $url")

            extractor.extractArticleAsync(url) { asyncResult ->
                asyncResult.result?.let { extractedItem(extractor, url, it, asyncResult, callback) }
                asyncResult.error?.let { callback(asyncResult) }
            }
        }
    }

    private fun extractedItem(extractor: IArticleExtractor, url: String, extractionResult: ItemExtractionResult, originalAsyncResult: AsyncResult<ItemExtractionResult>,
                              callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        if(extractionResult.couldExtractContent || articleExtractors.isDefaultExtractor(extractor)) {
            addDefaultData(extractor, url, extractionResult) { callback(originalAsyncResult) }
        }
        else { // couldExtractContent == false and extractor != defaultExtractor -> try with default extractor
            articleExtractors.defaultExtractor.extractArticleAsync(url) { newAsyncResult ->
                newAsyncResult.result?.let { addDefaultData(extractor, url, it) { callback(newAsyncResult) } }
                newAsyncResult.error?.let { addDefaultData(extractor, url, extractionResult) { callback(originalAsyncResult) } } // ok, then use original result
            }
        }
    }

    /**
     * Do to legal issues, don't extract an article from a url the user didn't for sure see before. Show her/him original web page before
     */
    open fun extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url: String, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        val extractionResult = ItemExtractionResult(Item(""), Source(url, url))

        addDefaultData(url, extractionResult) {
            callback(AsyncResult(true, result = extractionResult))
        }
    }

    fun extractArticleUserDidSeeBefore(extractionResult: ItemExtractionResult, html: String, url: String) {
        articleExtractors.getExtractorForUrl(url)?.let { extractor ->
            log.info("Using $extractor to extract html from url $url")

            extractor.parseHtml(extractionResult, html, url)

            if(extractionResult.couldExtractContent == false && articleExtractors.isDefaultExtractor(extractor) == false) {
                articleExtractors.defaultExtractor.parseHtml(extractionResult, html, url)
            }
        }
    }


    private fun addDefaultData(item: ArticleSummaryItem, extractionResult: ItemExtractionResult, callback: () -> Unit) {
        item.articleSummaryExtractorConfig?.tagsToAddOnExtractedArticles?.forEach {
            extractionResult.tags.add(it)
        }

        val siteName = getSiteName(item, extractionResult)

        addDefaultDataForSiteName(siteName, extractionResult, callback)
    }

    private fun addDefaultData(extractor: IArticleExtractor, item: ArticleSummaryItem, extractionResult: ItemExtractionResult, callback: () -> Unit) {
        item.articleSummaryExtractorConfig?.tagsToAddOnExtractedArticles?.forEach {
            extractionResult.tags.add(it)
        }

        val siteName = getSiteName(extractor, item, extractionResult)

        addDefaultDataForSiteName(siteName, extractionResult, callback)
    }

    private fun addDefaultData(url: String, extractionResult: ItemExtractionResult, callback: () -> Unit) {
        val siteName = getSiteNameForUrl(url)

        addDefaultDataForSiteName(siteName, extractionResult, callback)
    }

    private fun addDefaultData(extractor: IArticleExtractor, url: String, extractionResult: ItemExtractionResult, callback: () -> Unit) {
        val siteName = getSiteName(extractor, url, extractionResult)

        addDefaultDataForSiteName(siteName, extractionResult, callback)
    }

    private fun addDefaultDataForSiteName(siteName: String?, extractionResult: ItemExtractionResult, callback: () -> Unit) {
        if(siteName == null) {
            callback()
        }
        else {
            setSeries(extractionResult, siteName, callback)
        }
    }

    private fun getSiteName(item: ArticleSummaryItem, extractionResult: ItemExtractionResult): String? {
        var siteName = item.articleSummaryExtractorConfig?.name

        if(siteName == null) {
            siteName = extractionResult.seriesTitle
        }

        if(siteName == null) {
            siteName = getSiteNameForUrl(item.url)
        }

        return siteName
    }

    private fun getSiteName(extractor: IArticleExtractor, item: ArticleSummaryItem, extractionResult: ItemExtractionResult): String? {
        var siteName = getSiteName(extractor, item.url, extractionResult)

        if(siteName == null) {
            siteName = item.articleSummaryExtractorConfig?.name
        }

        return siteName
    }

    private fun getSiteName(extractor: IArticleExtractor, urlString: String, extractionResult: ItemExtractionResult): String? {
        var siteName = extractor.getName()

        if(siteName == null) {
            siteName = extractionResult.seriesTitle
        }

        if(siteName.isNullOrBlank()) { // when extractor is default article extractor, use host name for default tag and series
            siteName = getSiteNameForUrl(urlString)
        }

        return siteName
    }

    private fun getSiteNameForUrl(urlString: String): String? {
        try {
            val url = URL(urlString)

            return url.host.toLowerCase().replace("www.", "")
        } catch(ignored: Exception) { }

        return null
    }


    private fun setSeries(extractionResult: ItemExtractionResult, siteName: String, callback: () -> Unit) {
        if(extractionResult.series == null || extractionResult.series?.isPersisted() == false) { // series not set to a persisted Series -> try to find an existing one or create and persist a new one
            val seriesTitle = extractionResult.series?.title ?: siteName

            getSeriesForTitleSynchronized(seriesTitle) {
                extractionResult.series = it
                callback()
            }

            return // avoid that callback() at end of this method gets called
        }

        callback()
    }

    /**
     * To avoid that when multiple items get fetched in parallel that multiple tags get created for one extractor this method synchronizes access to getTagForExtractorName()
     */
    private fun getSeriesForTitleSynchronized(seriesTitle: String, callback: (Series) -> Unit) {
        synchronized(this) {
            val countDownLatch = CountDownLatch(1)

            getSeriesForTitle(seriesTitle) {
                countDownLatch.countDown()

                callback(it)
            }

            try { countDownLatch.await(1, TimeUnit.SECONDS) } catch(ignored: Exception) { }
        }
    }

    private fun getSeriesForTitle(seriesTitle: String, callback: (Series) -> Unit) {
        searchEngine.searchSeries(SeriesSearch(seriesTitle) { searchResults ->
            if(searchResults.isNotEmpty()) {
                val exactMatches = searchResults.filter { it.title == seriesTitle } // filter out that ones with a similar name, e.g. 'SZ Magazin' when searching for 'SZ'
                exactMatches.sortedByDescending { it.countSources }.firstOrNull()?.let {  // in case there are several same names, use that one with most sources
                    callback(it)
                    return@SeriesSearch
                }
            }

            val series = Series(seriesTitle) // no Series with name 'seriesTitle' found -> create new one

            seriesService.persist(series)

            callback(series)
        })
    }

}
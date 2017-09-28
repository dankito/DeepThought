package net.dankito.deepthought.news.article

import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.newsreader.article.ArticleExtractors
import net.dankito.newsreader.article.IArticleExtractor
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.service.data.SeriesService
import net.dankito.service.data.TagService
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.SeriesSearch
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ArticleExtractorManager(private val tagService: TagService, private val seriesService: SeriesService, private val searchEngine: ISearchEngine) {

    @Inject
    protected lateinit var articleExtractors: ArticleExtractors


    init {
        CommonComponent.component.inject(this)
    }


    fun extractArticleAndAddDefaultDataAsync(item: ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        articleExtractors.getExtractorForItem(item)?.let { extractor ->
            extractor.extractArticleAsync(item) { asyncResult ->
                asyncResult.result?.let { addDefaultData(extractor, item, asyncResult, it, callback) }
                asyncResult.error?.let { callback(asyncResult) }
            }
        }
    }

    fun extractArticleAndAddDefaultDataAsync(url: String, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        articleExtractors.getExtractorForUrl(url)?.let { extractor ->
            extractor.extractArticleAsync(url) { asyncResult ->
                asyncResult.result?.let { addDefaultData(extractor, url, asyncResult, it, callback) }
                asyncResult.error?.let { callback(asyncResult) }
            }
        }
    }

    fun extractArticleAndAddDefaultDataAsync(extractionResult: EntryExtractionResult, html: String, url: String) {
        articleExtractors.getExtractorForUrl(url)?.let { extractor ->
            extractor.parseHtml(extractionResult, html, url)
        }
    }

    private fun addDefaultData(extractor: IArticleExtractor, item: ArticleSummaryItem, asyncResult: AsyncResult<EntryExtractionResult>,
                                 extractionResult: EntryExtractionResult, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        val siteName = getSiteName(extractor, item)

        addDefaultDataForSiteName(siteName, callback, asyncResult, extractionResult)

        item.articleSummaryExtractorConfig?.tagsToAddOnExtractedArticles?.forEach {
            extractionResult.tags.add(it)
        }
    }

    private fun addDefaultData(extractor: IArticleExtractor, url: String, asyncResult: AsyncResult<EntryExtractionResult>,
                               extractionResult: EntryExtractionResult, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        val siteName = getSiteName(extractor, url)

        addDefaultDataForSiteName(siteName, callback, asyncResult, extractionResult)
    }

    private fun addDefaultDataForSiteName(siteName: String?, callback: (AsyncResult<EntryExtractionResult>) -> Unit, asyncResult: AsyncResult<EntryExtractionResult>, extractionResult: EntryExtractionResult) {
        if(siteName == null) {
            callback(asyncResult)
        }
        else {
            setSeries(extractionResult, siteName, asyncResult, callback)
        }
    }

    private fun getSiteName(extractor: IArticleExtractor, item: ArticleSummaryItem): String? {
        var siteName = getSiteName(extractor, item.url)

        if(siteName == null) {
            siteName = item.articleSummaryExtractorConfig?.name
        }

        return siteName
    }

    private fun getSiteName(extractor: IArticleExtractor, urlString: String): String? {
        var siteName = extractor.getName()

        if(siteName == null) { // when extractor is default article extractor, use host name for default tag and series
            try {
                val url = URL(urlString)
                siteName = url.host.toLowerCase().replace("www.", "")
            } catch(ignored: Exception) { }
        }

        return siteName
    }


    private fun setSeries(extractionResult: EntryExtractionResult, siteName: String, asyncResult: AsyncResult<EntryExtractionResult>, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        if(extractionResult.series == null || extractionResult.series?.isPersisted() == false) { // series not set to a persisted Series -> try to find an existing one or create and persist a new one
            val seriesTitle = extractionResult.series?.title ?: siteName

            getSeriesForTitleSynchronized(seriesTitle) {
                extractionResult.series = it
                callback(asyncResult)
            }

            return // avoid that callback() at end of this method gets called
        }

        callback(asyncResult)
    }

    /**
     * To avoid that when multiple entries get fetched in parallel that multiple tags get created for one extractor this method synchronizes access to getTagForExtractorName()
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
                callback(searchResults.first())
                return@SeriesSearch
            }

            val series = Series(seriesTitle) // no Series with name 'seriesTitle' found -> create new one

            seriesService.persist(series)

            callback(series)
        })
    }

}
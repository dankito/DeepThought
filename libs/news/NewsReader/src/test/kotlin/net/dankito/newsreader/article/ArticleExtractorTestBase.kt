package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.newsreader.model.EntryExtractionResult
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class ArticleExtractorTestBase {

    private val underTest: IArticleExtractor

    init {
        underTest = createArticleExtractor(OkHttpWebClient())
    }

    abstract fun createArticleExtractor(webClient: IWebClient): IArticleExtractor


    protected open fun getAndTestArticle(url: String, title: String, abstract: String?, previewImageUrl: String? = null) {
        val article = getArticle(url)

        testArticle(article, url, title, abstract, previewImageUrl)
    }

    protected open fun getArticle(url: String) : EntryExtractionResult? {
        var extractionResult: EntryExtractionResult? = null
        val countDownLatch = CountDownLatch(1)

        underTest.extractArticleAsync(url) {
            extractionResult = it.result

            countDownLatch.countDown()
        }

        countDownLatch.await(20, TimeUnit.SECONDS)

        return extractionResult
    }

    protected open fun testArticle(extractionResult: EntryExtractionResult?, url: String, title: String, abstract: String?, previewImageUrl: String? = null) {
        assertThat(extractionResult, notNullValue())

        extractionResult?.let { extractionResult ->
            assertThat(extractionResult.entry.content.isNullOrBlank(), `is`(false))
            assertThat(extractionResult.entry.abstractString, `is`(abstract))

            assertThat(extractionResult.reference, notNullValue())

            extractionResult.reference?.let { reference ->
                assertThat(reference.url, `is`(url))
                assertThat(reference.title, `is`(title))
                assertThat(reference.publishingDate, notNullValue())

                // TODO: handle previewImageUrl
//                assertThat(extractionResult.previewImageUrl, notNullValue())
//                previewImageUrl?.let { assertThat(extractionResult.previewImageUrl, `is`(previewImageUrl)) }
            }
        }
    }

}
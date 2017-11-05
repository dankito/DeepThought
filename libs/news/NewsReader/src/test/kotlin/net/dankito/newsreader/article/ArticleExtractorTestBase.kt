package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.OkHttpWebClient
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class ArticleExtractorTestBase {

    private val underTest: IArticleExtractor

    init {
        underTest = createArticleExtractor(OkHttpWebClient())
    }

    abstract fun createArticleExtractor(webClient: IWebClient): IArticleExtractor


    protected open fun getAndTestArticle(url: String, title: String, abstract: String?, previewImageUrl: String? = null, minContentLength: Int? = null,
                                         canPublishingDateBeNull: Boolean = false, subTitle: String? = null) {
        val article = getArticle(url)

        testArticle(article, url, title, abstract, previewImageUrl, minContentLength, canPublishingDateBeNull, subTitle)
    }

    protected open fun getArticle(url: String) : ItemExtractionResult? {
        var extractionResult: ItemExtractionResult? = null
        val countDownLatch = CountDownLatch(1)

        underTest.extractArticleAsync(url) {
            extractionResult = it.result

            countDownLatch.countDown()
        }

        countDownLatch.await(20, TimeUnit.SECONDS)

        return extractionResult
    }

    protected open fun testArticle(extractionResult: ItemExtractionResult?, url: String, title: String, abstract: String?, previewImageUrl: String? = null, minContentLength: Int? = null, canPublishingDateBeNull: Boolean = false, subTitle: String?) {
        assertThat(extractionResult, notNullValue())

        extractionResult?.let {
            assertThat(extractionResult.couldExtractContent, `is`(true))
            assertThat(extractionResult.error, nullValue())

            assertThat(extractionResult.item.content.isNullOrBlank(), `is`(false))
            abstract?.let { assertThat(extractionResult.item.summary, `is`(abstract)) }

//            previewImageUrl?.let { assertThat(extractionResult.source?.previewImageUrl, `is`(previewImageUrl)) }
            previewImageUrl?.let { assertThat(extractionResult.source?.previewImageUrl.isNullOrBlank(), `is`(false)) }
            minContentLength?.let { assertThat(extractionResult.item.content.length, `is`(greaterThan(it))) }

            assertThat(extractionResult.source, notNullValue())

            extractionResult.source?.let { reference ->
                assertThat(reference.url, `is`(url))
                assertThat(reference.title, `is`(title))
                if(canPublishingDateBeNull == false) {
                    assertThat(reference.publishingDate, notNullValue())
                }

                subTitle?.let { assertThat(reference.subTitle, `is`(subTitle)) }
            }
        }
    }

}
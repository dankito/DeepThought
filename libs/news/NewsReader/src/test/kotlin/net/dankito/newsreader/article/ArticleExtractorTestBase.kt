package net.dankito.newsreader.article

import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers.greaterThan
import org.jsoup.Jsoup
import org.junit.Assert.assertThat
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class ArticleExtractorTestBase {

    abstract fun createArticleExtractor(webClient: IWebClient): IArticleExtractor


    protected val underTest: IArticleExtractor = createArticleExtractor(createWebClient())


    protected open fun getAndTestArticle(url: String, title: String, summary: String?, previewImageUrl: String? = null, minContentLength: Int? = null,
                                         canPublishingDateBeNull: Boolean = false, subTitle: String? = null, fromDownloadedFile: String? = null, 
                                         saveResultToFile: String? = null): ItemExtractionResult? {
        val article = getArticle(url, fromDownloadedFile)

        if (saveResultToFile != null && article?.item != null) {
            getResultDestinationFile(saveResultToFile).writeText(article.item.content)
        }

        testArticle(article, url, title, summary, previewImageUrl, minContentLength, canPublishingDateBeNull, subTitle)

        return article
    }

    private fun getArticle(url: String, fromDownloadedFile: String?): ItemExtractionResult? {
        if (fromDownloadedFile != null) {
            val savedFile = getSavedFilePath(fromDownloadedFile)
            if (savedFile.exists()) {
                (underTest as? ArticleExtractorBase)?.let {
                    return underTest.extractArticle(url, Jsoup.parse(savedFile.readText()))
                }
            }
        }

        return getArticle(url)
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

    protected open fun testArticle(extractionResult: ItemExtractionResult?, url: String, title: String, summary: String?, previewImageUrl: String? = null, minContentLength: Int? = null, canPublishingDateBeNull: Boolean = false, subTitle: String?) {
        assertThat(extractionResult, notNullValue())

        extractionResult?.let {
            assertThat(extractionResult.couldExtractContent, `is`(true))
            assertThat(extractionResult.error, nullValue())

            assertThat(extractionResult.item.content.isNullOrBlank(), `is`(false))
            assertThat(extractionResult.item.summary.isNullOrBlank(), `is`(true))

//            previewImageUrl?.let { assertThat(extractionResult.source?.previewImageUrl, `is`(previewImageUrl)) }
            previewImageUrl?.let { assertThat(extractionResult.source?.previewImageUrl.isNullOrBlank(), `is`(false)) }
            minContentLength?.let { assertThat(extractionResult.item.content.length, `is`(greaterThan(it))) }

            assertThat(extractionResult.source, notNullValue())

            extractionResult.source?.let { source ->
                assertThat(source.url, `is`(url))
                assertThat(source.title, `is`(title))
                if(canPublishingDateBeNull == false) {
                    assertThat(source.publishingDate, notNullValue())
                }

                subTitle?.let { assertThat(source.subTitle, `is`(subTitle)) }
            }
        }
    }

    protected open fun downloadAndSaveArticleTo(destinationFile: String, url: String) {
        val file = getSavedFilePath(destinationFile)

        val webClient = createWebClient()

        val response = webClient.get(url)
        if (response.isSuccessful) {
            file.writeText(response.body!!)
        }
    }

    protected open fun getSavedFilePath(filename: String): File {
        val file = File(getSavedFilesFolder(), filename)

        return ensureFileHasHtmlFileExtension(file)
    }

    protected open fun getResultDestinationFile(filename: String): File {
        var file = File(getSavedFilesFolder(), filename)

        if (file.nameWithoutExtension.endsWith("_Result") == false) {
            file = File(file.parentFile, file.nameWithoutExtension + "_Result")
        }

        return ensureFileHasHtmlFileExtension(file)
    }

    protected open fun getSavedFilesFolder() =
        File("Downloaded", underTest.getName() ?: "Unknown_Extractor").apply { 
            this.mkdirs()
        }

    protected open fun ensureFileHasHtmlFileExtension(file: File): File {
        if (file.extension != "html" && file.extension != "htm") {
            return File(file.parentFile, file.nameWithoutExtension + ".html")
        }

        return file
    }

    protected open fun createWebClient() = OkHttpWebClient()

}
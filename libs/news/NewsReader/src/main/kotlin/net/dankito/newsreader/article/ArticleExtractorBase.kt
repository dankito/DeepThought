package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.getPlainTextForHtml
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.model.ArticleSummaryItem
import net.dankito.newsreader.model.LoginResult
import net.dankito.utils.AsyncResult
import net.dankito.utils.credentials.ICredentials
import net.dankito.utils.services.network.ExtractorBase
import net.dankito.utils.web.client.Cookie
import net.dankito.utils.web.client.IWebClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


abstract class ArticleExtractorBase(webClient: IWebClient) : ExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val StartsWithHttpOrHttpsMatcher = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE)

        private val log = LoggerFactory.getLogger(ArticleExtractorBase::class.java)
    }


    private val metaDataExtractor = WebPageMetaDataExtractor(webClient)

    private var isTryingToRetrieveFullArticle = false


    open val doesSupportLoggingIn: Boolean = false

    protected var loginResult: LoginResult? = null


    override fun extractArticleAsync(item : ArticleSummaryItem, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        extractArticleAsync(item.url) { extractionResult ->
            extractionResult.result?.let {
                setInfoFromArticleSummaryItemOnExtractionResult(item, it)
            }

            callback(extractionResult)
        }
    }

    private fun setInfoFromArticleSummaryItemOnExtractionResult(item: ArticleSummaryItem, extractionResult: ItemExtractionResult) {
        // do not set summary anymore, neither here nor in parseMetaData()

        extractionResult.source?.let { source ->
            if (source.previewImageUrl == null) {
                source.previewImageUrl = item.previewImageUrl
            }
        }
    }


    override fun extractArticleAsync(url : String, callback: (AsyncResult<ItemExtractionResult>) -> Unit) {
        thread {
            try {
                val extractionResult = extractArticle(url)

                if(extractionResult != null) {
                    extractionResult.source?.url = url // explicitly set source's url as for multipage articles article may gets extracted from a url different than url parameter
                    extractionResult.source?.lastAccessDate = Date()

                    callback(AsyncResult(true, result = extractionResult))
                }
                else {
                    callback(AsyncResult(false, Exception("Could not extract article from url $url"))) // TODO: localize
                }
            } catch(e: Exception) {
                log.error("Could not extract article from " + url, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    protected open fun extractArticle(url: String): ItemExtractionResult? {
        requestUrl(url).let { document ->
            val contentHtml = document.outerHtml()
            val extractionResult = ItemExtractionResult(Item(contentHtml), Source(url, url, null))
            parseMetaData(extractionResult, document)

            parseHtmlToArticle(extractionResult, document, url)

            // TODO: if extraction didn't work try default ArticleExtraction if not done already

            return extractionResult
        }
    }

    protected open fun extractArticleWithPost(extractionResult: ItemExtractionResult, url: String, body: String? = null, contentType: String? = null) {
        try {
            requestUrlWithPost(url, body, contentType).let { document ->
                parseHtmlToArticle(extractionResult, document, url)
            }
        } catch (e: Exception) {
            extractionResult.error = e
            log.error("Could not extract article with post from " + url, e)
        }
    }

    override fun parseHtml(extractionResult: ItemExtractionResult, html: String, url: String) {
        val document = Jsoup.parse(html, url)
        parseMetaData(extractionResult, document)

        parseHtmlToArticle(extractionResult, document, url)
    }

    abstract protected fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String)


    private fun parseMetaData(extractionResult: ItemExtractionResult, document: Document) {
        val metaData = metaDataExtractor.extractMetaData(document)

        metaData.title?.let { extractionResult.source?.title = it.trim() }

        // do not set summary anymore, neither here nor in setInfoFromArticleSummaryItemOnExtractionResult()

        extractionResult.source?.previewImageUrl = metaData.previewImageUrl

        extractionResult.source?.setPublishingDate(metaData.publishingDate, metaData.publishingDateString)

        metaData.siteName?.let { extractionResult.seriesTitle = it }
    }


    protected fun makeLinksAbsolute(element: Element, url: String) {
        element.select("[href]").forEach { hrefElement ->
            hrefElement.attr("href", makeLinkAbsolute(hrefElement.attr("href"), url))
        }

        element.select("[src]").forEach { srcElement ->
            srcElement.attr("src", makeLinkAbsolute(srcElement.attr("src"), url))
        }

        element.select("[data-zoom-src]").forEach { srcElement ->
            srcElement.attr("data-zoom-src", makeLinkAbsolute(srcElement.attr("data-zoom-src"), url))
        }
    }


    protected fun adjustSourceElements(element: Element) {
        for(sourceElement in element.select("span.source")) {
            sourceElement.parent().appendChild(Element(org.jsoup.parser.Tag.valueOf("br"), element.baseUri()))
        }
    }


    protected fun removeEmptyParagraphs(contentElement: Element, classNamesToPreserve: Collection<String> = ArrayList()) {
        val preserveRegex = classNamesToPreserve.joinToString("|").toRegex(RegexOption.IGNORE_CASE)

        ArrayList(contentElement.select("p, div").toList()).forEach {
            if(it.html().getPlainTextForHtml().isNullOrBlank() && preserveRegex.containsMatchIn(it.className()) == false) {
                it.remove()
            }
        }
    }


    /**
     * Remove <noscript> elements which impede that <img>s get loaded
     */
    protected fun unwrapImagesFromNoscriptElements(element: Element) {
        unwrapNoscriptElements(element, "img")
    }

    /**
     * Unwraps all <noscript> elements that contain an element with a tag from tagNamesToUnwrap.
     * Unwrap means that the <noscript> element gets removed and its child elements get inserted at the same place (index) in its former parent.
     */
    protected fun unwrapNoscriptElements(element: Element, vararg tagNamesToUnwrap: String) {
        val tagNamesSelector = tagNamesToUnwrap.joinToString(",")

        element.select("noscript").forEach { noscript ->
            if(noscript.select(tagNamesSelector).size > 0) {
                noscript.unwrap()
            }
        }
    }


    protected fun isHttpOrHttpsUrlFromHost(url: String, expectedHostAndPath: String): Boolean {
        return startsWithHttpOrHttps(url) && url.contains(expectedHostAndPath) && url.length > expectedHostAndPath.length + 6
    }

    protected fun startsWithHttpOrHttps(hostAndPath: String): Boolean {
        return StartsWithHttpOrHttpsMatcher.matcher(hostAndPath).find()
    }


    protected open fun parseDateString(dateString: String, dateFormat: DateFormat) : Date? {
        try {
            return dateFormat.parse(dateString)
        } catch(e: Exception) { log.info("Could not parse date string '$dateString' with DateFormat $dateFormat") }

        return null
    }


    open fun login(credentials: ICredentials): LoginResult? {
        return null
    }

    protected open fun setLoginResult(loginCookies: List<Cookie>): LoginResult? {
        this.loginResult = LoginResult(loginCookies)

        return this.loginResult
    }

    protected open fun needsLoginToViewFullArticle(url: String, document: Document): Boolean {
        return false
    }

    protected open fun handleNeedsLoginToViewFullArticle(url: String, document: Document, extractionResult: ItemExtractionResult) {
        if (needsLoginToViewFullArticle(url, document)) {
            if (isTryingToRetrieveFullArticle == false) {
                isTryingToRetrieveFullArticle = true

                loginResult?.let { loginResult ->
                    try {
                        val parameters = createParametersForUrl(url)
                        parameters.cookies = loginResult.cookies

                        val response = webClient.get(parameters)
                        if (response.isSuccessful) {
                            response.body?.let { html ->
                                parseHtml(extractionResult, html, url)
                                isTryingToRetrieveFullArticle = false

                                return // could successfully retrieve and parse content with LoginResult
                            }
                        }
                    } catch (e: Exception) {
                        log.info("Could not get url '$url' with LoginResult $loginResult", e)
                    }
                }
            }

            extractionResult.needsLoginToViewFullArticle = true // in all other cases set needsLoginToViewFullArticle to true
        }

        isTryingToRetrieveFullArticle = false
    }

}
package net.dankito.newsreader.article

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.authentication.HeiseAuthenticator
import net.dankito.newsreader.model.LoginResult
import net.dankito.utils.credentials.ICredentials
import net.dankito.utils.web.client.IWebClient
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.text.SimpleDateFormat
import java.util.*


abstract class HeiseNewsAndDeveloperArticleExtractorBase(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val ContentFilterSelector = ".widget-werbung, .akwa-ad-container, .akwa-ad-container--native, .hinweis_anzeige" +
                "a-paternoster, a-ad, [name=Teads], .a-teaser-header__heading, .article-footer__content, [name=meldung.newsticker.bottom.zurstartseite], .a-pvgs, " +
                "a.comment-button"

        private val DateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        private val MultiPageMobileArticleDateTimeFormat = SimpleDateFormat("yyyy-MM-dd")
    }

    private val authenticator = HeiseAuthenticator(webClient)


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { article ->
            getReadAllOnOnePageUrl(article, url)?.let { allOnOnePageUrl ->
                extractArticle(allOnOnePageUrl)?.let {
                    if(it.couldExtractContent) {
                        extractionResult.setExtractedContent(it.item, it.source)
                        return
                    }
                }
            }

            article.select("header").first()?.let { header ->
                header.select(".article__heading, .article-header__heading, .a-article-header__title").first()?.text()?.let { title ->
                    parseArticle(extractionResult, header, article, url, title.trim())
                    return
                }
            }

            if(isMobileArticle(article, document, url)) {
                parseMobileArticle(extractionResult, article, url)
            }
        }
    }

    private fun getReadAllOnOnePageUrl(article: Element, siteUrl: String): String? {
        article.select(".a-toc__text--onepage").first()?.let { allOnOnePageAnchorElement ->
            return makeLinkAbsolute(allOnOnePageAnchorElement.attr("href"), siteUrl)
        }

        article.select(".pre-akwa-toc__item--onepage a.pre-akwa-toc__link").first()?.let { allOnOnePageAnchorElement ->
            return makeLinkAbsolute(allOnOnePageAnchorElement.attr("href"), siteUrl)
        }

        article.select(".article-pages-summary__onepage").first()?.let { allOnOnePageAnchorElement ->
            return makeLinkAbsolute(allOnOnePageAnchorElement.attr("href"), siteUrl)
        }

        return null
    }


    protected open fun parseArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String) {
        articleElement.select("article#meldung").first()?.let { articleMeldungElement -> // article#meldung is the new version
            parseArticleMeldungArticle(extractionResult, headerElement, articleMeldungElement, url, title)
            if(extractionResult.couldExtractContent) {
                return
            }
        }

        articleElement.select(".meldung_wrapper").first()?.let { contentElement ->
            parseMeldungWrapperArticle(extractionResult, headerElement, articleElement, contentElement, url, title)
            if(extractionResult.couldExtractContent) {
                return
            }
        }

        articleElement.select(".article-content").first()?.let { articleContentElement ->
            parseArticleContentArticle(extractionResult, headerElement, articleContentElement, url, title)
        }
    }

    // even newer version
    protected fun parseArticleMeldungArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleMeldungElement: Element, url: String, title: String) {
        var contentHtml = extractArticleMeldungContent(articleMeldungElement, url)

        val previewImageElement = articleMeldungElement.select(".article-image").firstOrNull()
        val summaryElement = articleMeldungElement.select(".article-content__lead, .article-header__lead, .a-article-header__lead").firstOrNull()

        if(previewImageElement != null || summaryElement != null) {
            previewImageElement?.let { unwrapImagesFromNoscriptElements(it) }

            contentHtml = "<div>" + (previewImageElement?.outerHtml() ?: "") + (summaryElement?.outerHtml() ?: "") + contentHtml + "</div>"
        }

        val item = Item(contentHtml)

        val publishingDate = extractPublishingDate(headerElement)
        val source = Source(title, url, publishingDate, subTitle = headerElement.select(".a-article-header__label").firstOrNull()?.text()?.trim() ?: "")

        previewImageElement?.let {
            source.previewImageUrl = makeLinkAbsolute(previewImageElement.select(".article-image img").first()?.attr("src") ?: "", url)
        }

        extractionResult.setExtractedContent(item, source)
    }


    // new version
    protected fun parseArticleContentArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleContentElement: Element, url: String, title: String) {
        val source = Source(title, url, extractPublishingDate(headerElement))
        articleContentElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            source.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        cleanContentElement(articleContentElement)

        makeLinksAbsolute(articleContentElement, url)
        unwrapImagesFromNoscriptElements(articleContentElement)

        extractionResult.setExtractedContent(Item(articleContentElement.outerHtml()), source)
    }


    // old version
    protected fun parseMeldungWrapperArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, contentElement: Element, url: String, title: String) {
        val item = Item(extractMeldungWrapperContent(articleElement, url))

        val publishingDate = extractPublishingDate(headerElement)
        val source = Source(title, url, publishingDate)
        source.previewImageUrl = makeLinkAbsolute(contentElement.select(".aufmacherbild img").first()?.attr("src") ?: "", url)

        extractionResult.setExtractedContent(item, source)
    }


    private fun isMobileArticle(article: Element, document: Document, url: String): Boolean {
        return url.contains("://m.heise.de/")
    }

    private fun parseMobileArticle(extractionResult: ItemExtractionResult, article: Element, url: String) {
        val source = extractMobileArticleSource(article, url)

        cleanContentElement(article)
        article.select("h1").remove()

        val content = article.html()

        extractionResult.setExtractedContent(Item(content), source)
    }

    protected fun cleanContentElement(contentElement: Element) {
        contentElement.select("h1, time, span.author, a.comments, .comment, .btn-toolbar, .whatsbroadcast-toolbar, #whatsbroadcast, " +
                ".btn-group, .whatsbroadcast-group, .shariff, .ISI_IGNORE, .article_meta, .widget-werbung, .ad_container, .ad_content, " +
                ".akwa-ad-container, .akwa-ad-container--native, a-ad, .pvgs, .a-pvgs, .a-pvg, " +
                "a.comment-button").remove()

        removeEmptyParagraphs(contentElement, Arrays.asList("video"))

        contentElement.select("a-collapse").forEach { collapseAnchor ->
            if (collapseAnchor.text().contains("heise online daily Newsletter", true)) { // remove Newsletter paragraphs
                collapseAnchor.remove()
            }
        }
    }

    private fun extractMobileArticleSource(article: Element, url: String): Source {
        val title = article.select("h1").first()?.text()?.trim() ?: ""

        val source = Source(title, url)

        article.select("figure.aufmacherbild img").first()?.let {
            source.previewImageUrl = makeLinkAbsolute(it.attr("src"), url)
        }
        article.select("time").first()?.let {
            source.publishingDate = parseIsoDateTimeString(it.attr("datetime"))
            if(source.publishingDate == null) {
                source.publishingDate = tryToParseMultiPageMobileArticleDate(it.attr("datetime"))
            }
        }

        return source
    }

    private fun tryToParseMultiPageMobileArticleDate(date: String?): Date? {
        try {
            date?.let {
                return MultiPageMobileArticleDateTimeFormat.parse(it)
            }
        } catch(ignored: Exception) { }

        return null
    }


    protected open fun extractArticleMeldungContent(articleElement: Element, url: String): String {
        return extractContent(articleElement, url, ".akwa-article__content, .article-content")
    }

    protected open fun extractMeldungWrapperContent(articleElement: Element, url: String): String {
        return extractContent(articleElement, url, ".meldung_wrapper")
    }

    protected open fun extractContent(articleElement: Element, url: String, contentSelector: String): String {
        return articleElement.select(contentSelector).first()?.children()?.filter { element ->
            shouldFilterElement(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
        ?: ""
    }

    protected open fun shouldFilterElement(element: Element): Boolean {
        return element.select(ContentFilterSelector).isNotEmpty()
                || containsOnlyComment(element)
                || isNewsletterBox(element)
    }

    protected open fun isNewsletterBox(element: Element): Boolean {
        return element.selectFirst("a-collapse") != null
                && element.text().contains("heise online daily Newsletter", true)
    }

    protected open fun containsOnlyComment(element: Element) : Boolean {
        return element.childNodeSize() == 3 && element.childNode(1) is Comment && element.childNode(0) is TextNode && element.childNode(2) is TextNode
    }

    protected open fun getContentElementHtml(element: Element, url: String) : String {
        unwrapImagesFromNoscriptElements(element)
        makeLinksAbsolute(element, url)

        if(element.hasClass("gallery") && element.hasClass("compact") && element.hasAttr("data-data-url")) {

        }

        return element.outerHtml()
    }


    protected fun extractPublishingDate(header: Element): Date? {
        header.select("time").first()?.let { dateTimeElement ->
            return DateTimeFormat.parse(dateTimeElement.attr("datetime"))
        }

        return null
    }


    override fun login(credentials: ICredentials): LoginResult? {
        authenticator.login(credentials)?.let { loginCookie ->
            return setLoginResult(listOf(loginCookie))
        }

        return super.login(credentials)
    }

}
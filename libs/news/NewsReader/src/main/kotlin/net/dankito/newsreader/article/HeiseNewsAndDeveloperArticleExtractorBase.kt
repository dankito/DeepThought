package net.dankito.newsreader.article

import net.dankito.util.web.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.text.SimpleDateFormat
import java.util.*


abstract class HeiseNewsAndDeveloperArticleExtractorBase(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val DateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        private val MultiPageMobileArticleDateTimeFormat = SimpleDateFormat("yyyy-MM-dd")
    }


    abstract protected fun parseArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String)


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
                header.select(".article__heading").first()?.text()?.let { title ->
                    parseArticle(extractionResult, header, article, url, title)
                    return
                }
            }

            if(isMobileArticle(article, document, url)) {
                parseMobileArticle(extractionResult, article, url)
            }
        }
    }

    private fun getReadAllOnOnePageUrl(article: Element, siteUrl: String): String? {
        article.select(".pre-akwa-toc__item--onepage a.pre-akwa-toc__link").first()?.let { allOnOnePageAnchorElement ->
            return makeLinkAbsolute(allOnOnePageAnchorElement.attr("href"), siteUrl)
        }

        article.select(".article-pages-summary__onepage").first()?.let { allOnOnePageAnchorElement ->
            return makeLinkAbsolute(allOnOnePageAnchorElement.attr("href"), siteUrl)
        }

        return null
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
                ".akwa-ad-container, .akwa-ad-container--native").remove()

        removeEmptyParagraphs(contentElement, Arrays.asList("video"))
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


    protected open fun extractContent(articleElement: Element, url: String): String {
        return articleElement.select(".meldung_wrapper").first()?.children()?.filter { element ->
            shouldFilterElement(element) == false
        }?.joinToString(separator = "") { getContentElementHtml(it, url) }
        ?: ""
    }

    protected open fun shouldFilterElement(element: Element): Boolean {
        return element.select(".widget-werbung, .akwa-ad-container, .akwa-ad-container--native, .hinweis_anzeige").isNotEmpty() || containsOnlyComment(element)
    }

    protected open fun containsOnlyComment(element: Element) : Boolean {
        return element.childNodeSize() == 3 && element.childNode(1) is Comment && element.childNode(0) is TextNode && element.childNode(2) is TextNode
    }

    protected open fun getContentElementHtml(element: Element, url: String) : String {
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

}
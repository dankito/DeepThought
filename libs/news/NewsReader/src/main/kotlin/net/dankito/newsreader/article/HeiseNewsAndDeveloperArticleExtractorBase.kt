package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.text.SimpleDateFormat
import java.util.*


abstract class HeiseNewsAndDeveloperArticleExtractorBase(webClient: IWebClient) : ArticleExtractorBase(webClient), IArticleExtractor {

    companion object {
        private val DateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }


    abstract protected fun parseArticle(extractionResult: EntryExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String)


    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { article ->
            getReadAllOnOnePageUrl(article, url)?.let { allOnOnePageUrl ->
                extractArticle(allOnOnePageUrl)?.let {
                    if(it.couldExtractContent) {
                        extractionResult.setExtractedContent(it.entry, it.reference)
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

    private fun parseMobileArticle(extractionResult: EntryExtractionResult, article: Element, url: String) {
        val reference = extractMobileArticleReference(article, url)

        val abstract = article.select("p.lead_text").first()?.text()?.trim() ?: ""

        article.select("h1, figure.aufmacherbild, time, span.author, a.comments, p.lead_text, .comment, .btn-toolbar .whatsbroadcast-toolbar, #whatsbroadcast, " +
                ".btn-group, .whatsbroadcast-group, .shariff, .ISI_IGNORE, .article_meta, .widget-werbung").remove()
        val content = article.html()

        extractionResult.setExtractedContent(Entry(content, abstract), reference)
    }

    private fun extractMobileArticleReference(article: Element, url: String): Reference {
        val title = article.select("h1").first()?.text()?.trim() ?: ""

        val reference = Reference(url, title)

        article.select("figure.aufmacherbild img").first()?.let {
            reference.previewImageUrl = makeLinkAbsolute(it.attr("src"), url)
        }
        article.select("time").first()?.let {
            reference.publishingDate = parseIsoDateTimeString(it.attr("datetime"))
        }

        return reference
    }


    protected open fun containsOnlyComment(element: Element) : Boolean {
        return element.childNodeSize() == 3 && element.childNode(1) is Comment && element.childNode(0) is TextNode && element.childNode(2) is TextNode
    }

    protected open fun getContentElementHtml(element: Element, url: String) : String {
        makeLinksAbsolute(element, url)
        return element.outerHtml()
    }


    protected fun extractPublishingDate(header: Element): Date? {
        header.select("time").first()?.let { dateTimeElement ->
            return DateTimeFormat.parse(dateTimeElement.attr("datetime"))
        }

        return null
    }

}
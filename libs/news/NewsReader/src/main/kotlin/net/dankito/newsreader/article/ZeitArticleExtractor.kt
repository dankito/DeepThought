package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ZeitArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val ZeitDateTimeFormat: DateFormat = SimpleDateFormat("dd. MMMMM yyyy HH:mm", Locale.GERMAN)
        private val ZeitDateTimeFormatWithComma: DateFormat = SimpleDateFormat("dd. MMMMM yyyy, HH:mm", Locale.GERMAN)

        private val log = LoggerFactory.getLogger(ZeitArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "Zeit"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.toLowerCase().contains("://www.zeit.de/") && url.length > "://www.zeit.de/".length + 5
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { articleElement ->
            val multiPageArticleArticleOnOnePageUrl = getArticleOnOnePageUrlForMultiPageArticles(articleElement)
            if(multiPageArticleArticleOnOnePageUrl != null) {
                extractArticle(multiPageArticleArticleOnOnePageUrl)?.let {
                    if(it.couldExtractContent) {
                        extractionResult.setExtractedContent(it.item, it.source)
                        return
                    }
                }
            }

            val articleEntry = createEntry(articleElement)

            val reference = createReference(url, articleElement)

            extractionResult.setExtractedContent(articleEntry, reference)
        }
    }

    protected fun getArticleOnOnePageUrlForMultiPageArticles(articleBodyElement: Element): String? {
        val articleTocOnesieElement = articleBodyElement.select(".article-toc__onesie").first()
        if (articleTocOnesieElement != null) {
            if ("a" == articleTocOnesieElement.nodeName())
                return articleTocOnesieElement.attr("href")
        }

        return null
    }

    private fun createEntry(articleBodyElement: Element): Item {
        val abstractString = articleBodyElement.select("div.summary").text()

        var content = ""

        // remove <noscript> elements which impede that <img>s in <figure> get loaded
        unwrapImagesFromNoscriptElements(articleBodyElement)
        articleBodyElement.select(".sharing-menu, .metadata").remove()

        for(articleElement in articleBodyElement.select("p.article__item, ul.article__item, figure.article__item, .article__subheading, .article-heading__podcast-player, " +
                ".article--video, div[data-collection-template], .gate--register, .gate")) { // .gate--register to show to user that you have to  register for viewing this article
            content += articleElement.outerHtml()

            if(articleElement.attr("data-collection-template").isBlank() == false) {
                content += loadDataCollection(articleElement)
            }
        }

        return Item(content, abstractString)
    }

    private fun loadDataCollection(articleElement: Element): String {
        var dataCollectionScriptsAndStyles = ""
        var nextSibling = articleElement.nextSibling()

        while(nextSibling != null) {
            if(nextSibling is Element && ("script" == nextSibling.tagName() || "link" == nextSibling.tagName())) {
                dataCollectionScriptsAndStyles += nextSibling.outerHtml()
            }
            else if(nextSibling is TextNode == false) {
                break
            }

            nextSibling = nextSibling.nextSibling()
        }

        return dataCollectionScriptsAndStyles
    }

    private fun createReference(articleUrl: String, articleBodyElement: Element): Source {
        val title = articleBodyElement.select(".article-heading__title").text()

        var subTitle = articleBodyElement.select(".article-heading__kicker").text()
        if(subTitle.isNullOrBlank()) {
            subTitle = articleBodyElement.select(".article-heading__series").text().trim()
        }

        val publishingDate = parseDate(articleBodyElement)

        val reference = Source(title, articleUrl, publishingDate, subTitle = subTitle)

        articleBodyElement.parent().select(".article__media-item").first()?.let { previewImageElement ->
            reference.previewImageUrl = previewImageElement.attr("src")
        }

        return reference
    }


    private fun parseDate(articleBodyElement: Element): Date? {
        articleBodyElement.select(".metadata__date").first()?.let { articleDateTimeElement ->
            parseZeitDateTimeFormat(articleDateTimeElement.text())?.let { return it }

            val publishingDateString = articleDateTimeElement.attr("datetime")
            if(publishingDateString.isNotBlank()) {
                return parseIsoDateTimeString(publishingDateString)
            }
        }

        return null
    }

    private fun parseZeitDateTimeFormat(articleDateTime: String): Date? {
        var articleDateTime = articleDateTime
        articleDateTime = articleDateTime.replace("&nbsp;", "")
        articleDateTime = articleDateTime.replace(" Uhr", "").trim { it <= ' ' }

        try {
            return ZeitDateTimeFormatWithComma.parse(articleDateTime)
        } catch (ignored: Exception) { }

        try {
            return ZeitDateTimeFormat.parse(articleDateTime)
        } catch (e: Exception) {
            log.error("Could not parse Zeit DateTime Format " + articleDateTime, e)
        }

        return null
    }
}

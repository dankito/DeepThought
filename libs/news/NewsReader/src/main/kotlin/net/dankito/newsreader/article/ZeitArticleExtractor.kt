package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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

    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { articleElement ->
            val multiPageArticleArticleOnOnePageUrl = getArticleOnOnePageUrlForMultiPageArticles(articleElement)
            if(multiPageArticleArticleOnOnePageUrl != null) {
                extractArticle(multiPageArticleArticleOnOnePageUrl)?.let {
                    if(it.couldExtractContent) {
                        extractionResult.setExtractedContent(it.entry, it.reference)
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

    private fun createEntry(articleBodyElement: Element): Entry {
        val abstractString = articleBodyElement.select("div.summary").text()

        var content = ""

        articleBodyElement.parent().select(".article__media-container").first()?.let { previewImageElement ->
            content += previewImageElement.outerHtml()
        }

        for(articleElement in articleBodyElement.select("p.article__item, .article__subheading")) { // articleBodyElement.select("p .paragraph .article__item")
            content += articleElement.outerHtml()
        }

        return Entry(content, abstractString)
    }

    private fun createReference(articleUrl: String, articleBodyElement: Element): Reference {
        val title = articleBodyElement.select(".article-heading__title").text()

        val subTitle = articleBodyElement.select(".article-heading__kicker").text()

        val publishingDate = parseDate(articleBodyElement)

        val reference = Reference(articleUrl, title, publishingDate, subTitle = subTitle)

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

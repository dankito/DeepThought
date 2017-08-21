package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.AsyncResult
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.newsreader.model.ArticleSummaryItem
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*


class TagesschauArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val TagesschauDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    }


    override fun getName(): String? {
        return "Tagesschau"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("http://www.tagesschau.de/") && url.length > "http://www.tagesschau.de/".length
    }


    override fun extractArticleAsync(item: ArticleSummaryItem, callback: (AsyncResult<EntryExtractionResult>) -> Unit) {
        super.extractArticleAsync(item) {
            it.result?.let { it.entry.abstractString = item.summary } // it's very hard to extract abstract from html code, so use that one from ArticleSummaryItem

            callback(it)
        }
    }

    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        document.body().select("#content .storywrapper").first()?.let { contentElement ->
            extractEntry(contentElement)?.let { entry ->
                val reference = extractReference(url, contentElement)

                return EntryExtractionResult(entry, reference)
            }
        }

        return null
    }

    private fun extractEntry(contentElement: Element): Entry? {
        contentElement.select(".sectionZ .modParagraph").first()?.let { articleContentElement ->
            val abstract = extractAbstract(articleContentElement)

            cleanArticleContentElement(articleContentElement)

            val content = articleContentElement.outerHtml()

            return Entry(content, abstract)
        }

        return null
    }

    private fun extractAbstract(articleContentElement: Element): String {
        var abstract = ""

        val firstChild = articleContentElement.child(0)
        if(isAbstractElement(firstChild)) {
            abstract = firstChild.child(0).text().trim()
            firstChild.remove()
        }

        return abstract
    }

    private fun isAbstractElement(firstChild: Element?): Boolean {
        return firstChild != null && firstChild.hasClass("text") &&
                firstChild.child(0) != null && firstChild.child(0).tagName() == "strong"
    }

    private fun cleanArticleContentElement(articleContentElement: Element) {
        articleContentElement.select(".autorenzeile, .socialMedia, .video .modForm, .linklist, .teaserImTeaser, .metablockwrapper").remove()

        articleContentElement.select(".teaser").forEach { teaserElement -> // remove teasers without Sendungsbezug
            teaserElement.select(".teasertext").first()?.let { teaserTextElement ->
                if(teaserTextElement.children().size > 0 && teaserTextElement.child(0).tagName() == "a") {
                    teaserElement.parent().remove()
                }
            }
        }

        articleContentElement.children().forEach { child -> // remove paragraphs with no texts
            if(child.text().trim().isEmpty()) {
                child.remove()
            }
        }
    }

    private fun extractReference(url: String, contentElement: Element): Reference? {
        contentElement.select(".sectionA .box").first()?.let { headerElement ->
            val title = headerElement.select(".headline").first()?.text()?.trim() ?: ""
            val subTitle = headerElement.select(".dachzeile").first()?.text()?.trim() ?: ""

            val publishingDate = extractPublishingDate(headerElement)

            val reference = Reference(url, title, publishingDate, subTitle = subTitle)

            headerElement.select(".media img").first()?.let { previewImageElement ->
                reference.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
            }

            return reference
        }

        return null
    }

    private fun extractPublishingDate(headerElement: Element): Date? {
        try {
            var publishingDateString = headerElement.select(".stand").first()?.text()?.replace("Stand: ", "")?.trim() ?: ""
            val dateEndIndex = publishingDateString.indexOf(' ')
            if (dateEndIndex > 0) {
                publishingDateString = publishingDateString.substring(0, dateEndIndex).trim()
            }

            return TagesschauDateFormat.parse(publishingDateString)
        } catch(e: Exception) { }

        return null
    }

}
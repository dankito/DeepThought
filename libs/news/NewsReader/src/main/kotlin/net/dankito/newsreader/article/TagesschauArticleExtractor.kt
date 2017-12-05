package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
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
        return isHttpOrHttpsUrlFromHost(url, "www.tagesschau.de/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("#content .storywrapper").first()?.let { contentElement ->
            extractEntry(contentElement)?.let { entry ->
                val reference = extractReference(url, contentElement)

                extractionResult.setExtractedContent(entry, reference)
            }
        }
    }

    private fun extractEntry(contentElement: Element): Item? {
        contentElement.select(".sectionZ .modParagraph").first()?.let { articleContentElement ->
            cleanArticleContentElement(articleContentElement)

            val content = articleContentElement.outerHtml()

            return Item(content)
        }

        return null
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

    private fun extractReference(url: String, contentElement: Element): Source? {
        contentElement.select(".sectionA .box").first()?.let { headerElement ->
            val title = headerElement.select(".headline").first()?.text()?.trim() ?: ""
            val subTitle = headerElement.select(".dachzeile").first()?.text()?.trim() ?: ""

            val publishingDate = extractPublishingDate(headerElement)

            val reference = Source(title, url, publishingDate, subTitle = subTitle)

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
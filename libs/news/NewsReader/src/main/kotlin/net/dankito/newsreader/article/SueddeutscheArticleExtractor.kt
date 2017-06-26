package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.newsreader.model.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class SueddeutscheArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        val SueddeutscheHeaderDateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }


    override fun getName(): String? {
        return "SZ"
    }

    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        document.body().select("#sitecontent").first()?.let { siteContent ->
            val reference = extractReference(siteContent, url)

            siteContent.select("#article-body").first()?.let { articleBody ->
                val abstract = articleBody.select(".entry-summary").first()?.html() ?: ""

                cleanArticleBody(articleBody)

                val entry = Entry(articleBody.html(), abstract)

                siteContent.select(".topenrichment figure img").first()?.let { entry.previewImageUrl = getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(it, "src", url) }

                return EntryExtractionResult(entry, reference)
            }
        }

        return null
    }

    private fun cleanArticleBody(articleBody: Element) {
        articleBody.select(".entry-summary, #article-sidebar-wrapper, .ad, .authors, .teaserable-layout").remove()
    }

    private fun extractReference(siteContent: Element, url: String): Reference? {
        siteContent.select(".header").first()?.let { headerElement ->
            headerElement.select("h2").first()?.let { heading ->
                var subTitle = ""
                heading.select("strong").first()?.let {
                    subTitle = it.text()
                    it.remove() // remove element so that it's not as well part of title
                }

                val publishingDate = extractPublishingDate(headerElement)

                return Reference(url, heading.text(), publishingDate, getName(), subTitle)
            }
        }

        return null
    }

    private fun extractPublishingDate(headerElement: Element): Date? {
        headerElement.select("time").first()?.let {
            try {
                val dateString = it.attr("datetime")
                return SueddeutscheHeaderDateFormat.parse(dateString)
            } catch(ignored: Exception) { }
        }

        return null
    }

}
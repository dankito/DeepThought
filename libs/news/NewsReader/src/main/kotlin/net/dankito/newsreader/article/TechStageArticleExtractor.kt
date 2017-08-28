package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*


class TechStageArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "TechStage"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("http") && url.contains("://www.techstage.de/") && url.length > "://www.techstage.de/".length + 4
    }


    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        document.body().select("#content > article").first()?.let { articleElement ->
            articleElement.select("#article_content").let { contentElement ->
                val reference = extractReference(articleElement, contentElement, url)

                val abstract = extractAbstract(contentElement)

                cleanContent(contentElement)

                return EntryExtractionResult(Entry(contentElement.outerHtml(), abstract), reference)
            }
        }

        return null
    }

    private fun extractAbstract(contentElement: Elements): String {
        var abstract = ""
        val abstractParagraph = contentElement.select("p > strong").first()
        abstractParagraph?.let {
            abstract = abstractParagraph.text().trim()
            abstractParagraph.remove()
        }
        return abstract
    }

    private fun cleanContent(contentElement: Elements) {
        contentElement.select("#article_comments, #article_navigation, .meta, .rectangle_ad, #pvg-deals-anchor, .article_tags_hl, .article_tags").remove()
    }


    private fun extractReference(articleElement: Element, contentElement: Elements, url: String): Reference {
        val title = articleElement.select("h1").first()?.text()?.trim() ?: ""

        var previewImageUrl: String? = null
        contentElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        var publishingDate: Date? = null
        contentElement.select(".meta time").first()?.let { timeElement ->
            publishingDate = parseIsoDateTimeString(timeElement.attr("datetime"))
        }

        val reference = Reference(url, title, publishingDate)
        reference.previewImageUrl = previewImageUrl

        return reference
    }

}
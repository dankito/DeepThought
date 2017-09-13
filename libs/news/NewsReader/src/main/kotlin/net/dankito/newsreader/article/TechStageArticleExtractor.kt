package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*


class TechStageArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "TechStage"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("http") && url.contains("://www.techstage.de/") && url.length > "://www.techstage.de/".length + 4
    }


    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        document.body().select("#content > article").first()?.let { articleElement ->
            articleElement.select("#article_content").first()?.let { contentElement ->
                val reference = extractReference(articleElement, contentElement, url)

                val abstract = extractAbstract(contentElement)

                cleanContent(contentElement)

                extractionResult.setExtractedContent(Entry(contentElement.outerHtml(), abstract), reference)
            }
        }
    }

    private fun extractAbstract(contentElement: Element): String {
        var abstract = ""
        val abstractParagraph = contentElement.select("p > strong").first()
        abstractParagraph?.let {
            abstract = abstractParagraph.text().trim()
            abstractParagraph.remove()
        }
        return abstract
    }

    private fun cleanContent(contentElement: Element) {
        contentElement.select("#article_comments, #article_navigation, .meta, .rectangle_ad, #pvg-deals-anchor, .pvgs, .article_tags_hl, .article_tags").remove()

        contentElement.parent().select("#article_content > aside").first()?.let { asideElement ->
            ArrayList(asideElement.children()).forEach { childElement ->
                if(childElement.hasClass("pvg-redaktion") == false) {
                    childElement.remove()
                }
            }
        }
    }


    private fun extractReference(articleElement: Element, contentElement: Element, url: String): Reference {
        val title = articleElement.select("h1").first()?.text()?.trim() ?: ""

        var previewImageUrl: String? = null
        contentElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        var publishingDate: Date? = null
        contentElement.select(".meta time").first()?.let { timeElement ->
            publishingDate = parseIsoDateTimeString(timeElement.attr("datetime"))
        }

        val reference = Reference(url, title, publishingDate, previewImageUrl)

        return reference
    }

}
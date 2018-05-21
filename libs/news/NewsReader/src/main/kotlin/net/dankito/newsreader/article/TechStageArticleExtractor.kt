package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*


class TechStageArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "TechStage"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.techstage.de/")
    }


    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("#content > article").first()?.let { articleElement ->
            articleElement.select("#article_content").first()?.let { contentElement ->
                val source = extractSource(articleElement, contentElement, url)

                cleanContent(contentElement)

                extractionResult.setExtractedContent(Item(contentElement.outerHtml()), source)
            }
        }
    }

    private fun cleanContent(contentElement: Element) {
        // TODO: remove asides ?
        contentElement.select("#article_comments, #article_navigation, .meta, .rectangle_ad, .ad_container, .ad_content, #pvg-deals-anchor, .pvgs, .a-pvgs, .a-pvg, " +
                ".techstage--aside-pvg-header, .article_tags_hl, .article_tags, .OUTBRAIN").remove()

        contentElement.parent().select("#article_content > aside").first()?.let { asideElement ->
            ArrayList(asideElement.children()).forEach { childElement ->
                if(childElement.hasClass("pvg-redaktion") == false) {
                    childElement.remove()
                }
            }
        }
    }


    private fun extractSource(articleElement: Element, contentElement: Element, url: String): Source {
        val title = articleElement.select("h1").first()?.text()?.trim() ?: ""

        var previewImageUrl: String? = null
        contentElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        var publishingDate: Date? = null
        contentElement.select(".meta time").first()?.let { timeElement ->
            publishingDate = parseIsoDateTimeString(timeElement.attr("datetime"))
        }

        val source = Source(title, url, publishingDate, previewImageUrl)

        return source
    }

}
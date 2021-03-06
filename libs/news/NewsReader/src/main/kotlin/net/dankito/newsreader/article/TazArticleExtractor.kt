package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat


class TazArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val tazDateFormat = SimpleDateFormat("d.M.yyyy")
    }


    override fun getName(): String? {
        return "taz"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "taz.de/")
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select(".sectbody").first()?.let { bodyElement ->
            val source = extractSource(document, url, bodyElement)

            bodyElement.select("h1, h4, .caption, .rack, .ad_bin, .contentad, .sold").remove()
            val content = bodyElement.children().joinToString("") { it.outerHtml() }

            extractionResult.setExtractedContent(Item(content), source)
        }
    }

    private fun extractSource(document: Document, url: String, bodyElement: Element): Source {
        var title = ""
        bodyElement.select("h1").first()?.let { title = it.text().trim() }

        var subTitle = ""
        bodyElement.select("h4").first()?.let { subTitle = it.text().trim() }

        val source = Source(title, url, subTitle = subTitle)

        bodyElement.select(".picture img").first()?.let { source.previewImageUrl = makeLinkAbsolute(it.attr("src"), url) }

        document.body().select(".wing .date").first()?.let { dateElement ->
            try {
                val dateString = dateElement.text().trim().replace(" ", "").replace(8201.toChar().toString(), "")
                source.publishingDate = tazDateFormat.parse(dateString)
            } catch(ignored: Exception) { }
        }

        return source
    }

}
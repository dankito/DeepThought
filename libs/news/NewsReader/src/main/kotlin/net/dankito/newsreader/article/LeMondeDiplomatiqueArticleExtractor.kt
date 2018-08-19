package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


class LeMondeDiplomatiqueArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val LeMondeDiplomatiqueDateFormat = SimpleDateFormat("yyyy-MM-dd")

        private val log = LoggerFactory.getLogger(LeMondeDiplomatiqueArticleExtractor::class.java)
    }


    override fun getName(): String? {
        return "Le Monde diplomatique"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.monde-diplomatique.fr/") ||  isHttpOrHttpsUrlFromHost(url, "mondediplo.com/")
    }




    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("#contenu").first()?.let { contentElement ->
            var content = contentElement.select(".chapodactu").first()?.outerHtml() ?: ""
            contentElement.select(".contenu-principal").first()?.let { mainContent ->
                mainContent.select(".bandeautitre").remove()
                content += mainContent.outerHtml()

                mainContent.select(".cartouche").first()?.let { cartouche ->
                    val source = extractSource(cartouche, document, url)

                    source.previewImageUrl?.let { previewImageUrl ->
                        if(mainContent.select("figure.xl").size == 0) { // for articles without image, show preview image at top
                            content = "<img src=\"$previewImageUrl\" > $content"
                        }
                    }

                    getScriptForPodcast(document, url)?.let {
                        content = it + content
                    }

                    extractionResult.setExtractedContent(Item(content), source)
                }
            }
        }
    }

    private fun getScriptForPodcast(document: Document, url: String): String? {
        var isPodcastArticle = false
        var podcastScripts = ""

        document.select("script").forEach { scriptElement ->
            if (scriptElement.attr("src").contains("soundmanager")) {
                podcastScripts += scriptElement.outerHtml()

                isPodcastArticle = true
            }
            else if (scriptElement.html().contains("jQuery")) {
                // to avoid 'Uncaught ReferenceError: photoswipe is not defined'
                podcastScripts += "<script type=\"text/javascript\">photoswipe = { };</script>" +
                        scriptElement.outerHtml()
            }
        }


        if(isPodcastArticle) {
            return podcastScripts
        }

        return null
    }

    private fun extractSource(cartouche: Element, document: Document, url: String): Source {
        val title = cartouche.select("h1").first()?.text() ?: ""
        val subTitle = cartouche.select("p").first()?.text() ?: ""
        val publishingDate = parsePublishingDate(document)

        var previewImageUrl: String? = null
        document.head().select("meta[property=og:image]").first()?.let { previewImageElement ->
            previewImageUrl = previewImageElement.attr("content")
        }
        val source = Source(title, url, publishingDate, previewImageUrl, subTitle)
        return source
    }

    private fun parsePublishingDate(document: Document): Date? {
        document.head().select("meta[property=article:published_time]").let { publishingDateElement ->
            try {
                return LeMondeDiplomatiqueDateFormat.parse(publishingDateElement.attr("content"))
            } catch(e: Exception) { log.warn("Could not parse Le Monde diplomatique date from ${publishingDateElement.attr("content")} ($publishingDateElement)", e) }
        }

        return null
    }

}
package net.dankito.newsreader.article.sites

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.newsreader.article.ArticleExtractorBase
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class WikipediaArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    override fun getName(): String? {
        return "Wikipedia"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.contains(".wikipedia.org/wiki/")
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        var title = ""
        var content = ""
        var summary = ""

        document.body().select(".heading-holder h1").first()?.let { heading ->
            title = heading.text().trim()
            content += heading.outerHtml()
        }

        document.body().select("#bodyContent").first()?.let { bodyContent ->
            bodyContent.select(".edit-page, #toc").remove() // of course i'd like to have to table of contents, but element is empty
            unwrapImagesFromNoscriptElements(bodyContent) // so that images get displayed
            makeLinksAbsolute(bodyContent, url)

            content += bodyContent.outerHtml()

            tryToExtractSummary(bodyContent)?.let { summary = it }
        }

        var previewImageUrl: String? = null
        document.body().select(".infobox img").first()?.let { infoImage ->
            previewImageUrl = makeLinkAbsolute(infoImage.attr("src"), url)
        }

        extractionResult.setExtractedContent(Item(content, summary), Source(title, url, previewImageUrl = previewImageUrl))
    }

    private fun tryToExtractSummary(bodyContent: Element): String? {
        bodyContent.select(".mw-parser-output").first()?.let { firstSection ->
            if(firstSection.children().size > 0) {
                val firstChild = firstSection.child(0)

                for(i in 0..firstChild.children().size - 2) {
                    val child = firstChild.child(i)
                    if(child.tagName() == "p") {
                        child.select("sup.reference").remove()
                        return child.text()
                    }
                }
            }
        }

        return null
    }

}
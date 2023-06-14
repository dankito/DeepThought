package net.dankito.newsreader.article

import net.dankito.utils.web.client.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class TelepolisArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {


    override fun getName(): String? {
        return "Telepolis"
    }

    override fun canExtractItemFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.heise.de/tp/features/") ||  isHttpOrHttpsUrlFromHost(url, "m.heise.de/tp/features/")
    }




    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().selectFirst("article")?.let { articleElement ->
            getReadAllOnOnePageUrl(articleElement, url)?.let { allOnOnePageUrl ->
                extractArticle(allOnOnePageUrl)?.let {
                    if(it.couldExtractContent) {
                        extractionResult.setExtractedContent(it.item, it.source)
                        return
                    }
                }
            }

            if (articleElement.hasClass("printversion")) {
                parsePrintVersionToArticle(extractionResult, articleElement, url)
                return
            }

            articleElement.selectFirst("header")?.let { headerElement ->
                headerElement.selectFirst(".article__heading")?.text()?.let { title ->
                    parseArticle(extractionResult, headerElement, articleElement, url, title)
                }
            }
        }
    }

    private fun getReadAllOnOnePageUrl(article: Element, siteUrl: String): String? =
        article.selectFirst(".pre-akwa-toc")?.let { tocElement -> // a multi page article
            tocElement.selectFirst("li.pre-akwa-toc__item--onepage a.pre-akwa-toc__link")?.let { allOnOnePageAnchor ->
                return makeLinkAbsolute(allOnOnePageAnchor.attr("href"), siteUrl)
            }

            // but unfortunately there's no 'Read all on one page' url so we use the print site
            article.select(".beitragsfooter_printversion")?.let { printAnchorElement ->
                return makeLinkAbsolute(printAnchorElement.attr("href"), siteUrl)
            }
        }


    override fun parseArticle(extractionResult: ItemExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String) {
        val source = extractSource(headerElement, articleElement, url, title)

        cleanContent(articleElement)
        transformElements(articleElement)
        makeLinksAbsolute(articleElement, url)
        val content = articleElement.children().joinToString("") { it.outerHtml()}

        extractionResult.setExtractedContent(Item(content), source)
    }

    private fun cleanContent(articleElement: Element) {
        articleElement.select("header, footer, .apester-media, .apester-element, .OUTBRAIN").remove()

        articleElement.select(".hinweis_anzeige").forEach { it.parent().remove() }

        ArrayList(articleElement.select("script")).forEach { script ->
            if(script.attr("src").contains("outbrain.com/")) {
                script.remove()
            }
        }
    }

    private fun transformElements(articleElement: Element) {
        articleElement.select("div.frage, div.antwort").tagName("p") // convert question / answer divs to paragraphs
    }

    private fun extractSource(headerElement: Element, articleElement: Element, url: String, title: String): Source? {
        val source = Source(title, url, extractPublishingDate(headerElement))

        articleElement.selectFirst(".aufmacherbild img")?.let { previewImageElement ->
            source.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        return source
    }


    private fun parsePrintVersionToArticle(extractionResult: ItemExtractionResult, articleElement: Element, url: String) {
        val source = extractSourceForPrintVersion(articleElement, url)

        // TODO: now we don't have the summary anymore (print version doesn't have it in Fliesstext) -> try to add it
        val content = extractContentForPrintVersion(articleElement, url)

        extractionResult.setExtractedContent(Item(content), source)
    }

    private fun extractContentForPrintVersion(articleElement: Element, url: String): String {
        var content = ""

        transformElements(articleElement)

        var contentElement = articleElement.selectFirst(".content")

        if(contentElement == null) { // it seems the print version has no .content element anymore
            if(articleElement.select("header, .printversion__back-to-article, .printversion__logo").size >= 3) { // ok, articleElement is already the contentElement -> remove clutter
                articleElement.select("header, .printversion__back-to-article, .printversion__logo, .printversion__copyright").remove()
                contentElement = articleElement
            }
        }

        contentElement?.let {
            makeLinksAbsolute(contentElement, url)
            tryToReplaceReferencesWithLinks(contentElement, articleElement)

            removeReferencesFromPrintVersion(contentElement)

            content = contentElement.outerHtml()
        }

        return content
    }

    private fun tryToReplaceReferencesWithLinks(contentElement: Element, articleElement: Element) {
        val references = extractPrintVersionReferenceNumbers(articleElement)

        contentElement.select("b").forEach { boldElement ->
            getReferenceNumber(boldElement)?.let { number ->
                references[number]?.let { referenceUrl ->
                    val anchorElement = Element("a")
                    anchorElement.attr("href", referenceUrl)
                    anchorElement.text(boldElement.text())

                    boldElement.replaceWith(anchorElement)
                }
            }
        }
    }

    private fun extractPrintVersionReferenceNumbers(articleElement: Element): HashMap<Int, String> {
        val references = HashMap<Int, String>()

        articleElement.select("p > strong").forEach {
            if(it.text().trim() == "Links in diesem Artikel:") {
                it.parent().select("small").forEach { smallElement ->
                    getReferenceNumber(smallElement)?.let { referenceNumber ->
                        references.put(referenceNumber, smallElement.text().trim())
                    }
                }
            }
        }
        return references
    }

    private fun getReferenceNumber(element: Element): Int? {
        val text = element.text().trim()
        val endIndex = text.indexOf(']')

        if(endIndex > 0) {
            val startIndex = text.indexOf('[')

            if(startIndex >= 0 && startIndex < endIndex) {
                try {
                    val referenceNumberString = text.substring(startIndex + 1, endIndex)
                    val referenceNumber = Integer.parseInt(referenceNumberString)

                    element.text(text.replace("[" + referenceNumberString + "]", ""))

                    return referenceNumber
                } catch(ignored: Exception) { }
            }
        }

        return null
    }

    private fun extractSourceForPrintVersion(articleElement: Element, url: String): Source {
        val title = articleElement.selectFirst("h1")?.text()?.trim() ?: ""
        val publishingDate = articleElement.selectFirst(".publish-info")?.let { extractPublishingDate(it) }

        val source = Source(title, url, publishingDate)

        articleElement.selectFirst(".aufmacherbild img")?.let { previewImageElement ->
            source.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }
        articleElement.ownerDocument().head().selectFirst("meta[property=og:image]")?.attr("content")?.let {
            source.previewImageUrl = it
        }

        return source
    }

    private fun removeReferencesFromPrintVersion(articleElement: Element) {
        articleElement.select("p > strong").forEach {
            if(it.text().trim() == "Links in diesem Artikel:" || it.text().trim() == "URL dieses Artikels:") {
                it.parent().remove()
            }
        }

        articleElement.select("hr").remove()
    }

}
package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class TelepolisArticleExtractor(webClient: IWebClient) : HeiseNewsAndDeveloperArticleExtractorBase(webClient) {


    override fun getName(): String? {
        return "Telepolis"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return url.startsWith("https://www.heise.de/tp/features/")
    }




    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { articleElement ->
            getReadAllOnOnePageUrl(articleElement, url)?.let { allOnOnePageUrl ->
                extractArticle(allOnOnePageUrl)?.let {
                    if(it.couldExtractContent) {
                        extractionResult.setExtractedContent(it.entry, it.reference)
                        return
                    }
                }
            }

            if(articleElement.hasClass("printversion")) {
                parsePrintVersionToArticle(extractionResult, articleElement, url)
                return
            }

            articleElement.select("header").first()?.let { headerElement ->
                headerElement.select(".article__heading").first()?.text()?.let { title ->
                    parseArticle(extractionResult, headerElement, articleElement, url, title)
                }
            }
        }
    }

    private fun getReadAllOnOnePageUrl(article: Element, siteUrl: String): String? {
        article.select(".pre-akwa-toc").first()?.let { // a multi page article
            // but unfortunately there's no 'Read all on one page' url so we use the print site
            article.select(".beitragsfooter_printversion")?.let { printAnchorElement ->
                return makeLinkAbsolute(printAnchorElement.attr("href"), siteUrl)
            }
        }

        return null
    }


    override fun parseArticle(extractionResult: EntryExtractionResult, headerElement: Element, articleElement: Element, url: String, title: String) {
        val reference = extractReference(headerElement, articleElement, url, title)

        val abstract = articleElement.select(".beitraganriss").first()?.text()?.trim() ?: ""

        articleElement.select("header, footer, .beitraganriss").remove()
        makeLinksAbsolute(articleElement, url)
        val content = articleElement.children().joinToString("") { it.outerHtml()}

        extractionResult.setExtractedContent(Entry(content, abstract), reference)
    }

    private fun extractReference(headerElement: Element, articleElement: Element, url: String, title: String): Reference? {
        val reference = Reference(url, title, extractPublishingDate(headerElement))

        articleElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            reference.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        return reference
    }


    private fun parsePrintVersionToArticle(extractionResult: EntryExtractionResult, articleElement: Element, url: String) {
        val reference = extractReferenceForPrintVersion(articleElement, url)

        val abstract = articleElement.ownerDocument().head().select("meta[property=og:description]").first()?.attr("content")?.trim() ?: ""

        val content = extractContentForPringVersion(articleElement, url)

        extractionResult.setExtractedContent(Entry(content, abstract), reference)
    }

    private fun extractContentForPringVersion(articleElement: Element, url: String): String {
        var content = ""

        articleElement.select(".content").first()?.let { contentElement ->
            makeLinksAbsolute(contentElement, url)
            tryToReplaceReferencesWithLinks(contentElement, articleElement)

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

    private fun extractReferenceForPrintVersion(articleElement: Element, url: String): Reference {
        val title = articleElement.select("h1").first()?.text()?.trim() ?: ""
        val publishingDate = articleElement.select(".publish-info").first()?.let { extractPublishingDate(it) }

        val reference = Reference(url, title, publishingDate)

        articleElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            reference.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }
        articleElement.ownerDocument().head().select("meta[property=og:image]").first()?.attr("content")?.let {
            reference.previewImageUrl = it
        }

        return reference
    }

}
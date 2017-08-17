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




    override fun parseHtmlToArticle(document: Document, url: String): EntryExtractionResult? {
        document.body().select("article").first()?.let { articleElement ->
            getReadAllOnOnePageUrl(articleElement, url)?.let { allOnOnePageUrl ->
                return extractArticle(allOnOnePageUrl)
            }

            articleElement.select("header").first()?.let { headerElement ->
                headerElement.select(".article__heading").first()?.text()?.let { title ->
                    return parseArticle(headerElement, articleElement, url, title)
                }
            }

            if(articleElement.hasClass("printversion")) {
                return parsePrintVersionToArticle(articleElement, url)
            }
        }

        return null
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


    override fun parseArticle(headerElement: Element, articleElement: Element, url: String, title: String): EntryExtractionResult? {
        val reference = extractReference(headerElement, articleElement, url, title)

        val abstract = articleElement.select(".beitraganriss").first()?.text()?.trim() ?: ""

        articleElement.select("header, footer, .beitraganriss").remove()
        makeLinksAbsolute(articleElement, url)
        val content = articleElement.children().joinToString("") { it.outerHtml()}

        return EntryExtractionResult(Entry(content, abstract), reference)
    }

    private fun extractReference(headerElement: Element, articleElement: Element, url: String, title: String): Reference? {
        val reference = Reference(url, title, extractPublishingDate(headerElement))

        articleElement.select(".aufmacherbild img").first()?.let { previewImageElement ->
            reference.previewImageUrl = makeLinkAbsolute(previewImageElement.attr("src"), url)
        }

        return reference
    }


    private fun parsePrintVersionToArticle(articleElement: Element, url: String): EntryExtractionResult? {
        val reference = extractReferenceForPrintVersion(articleElement, url)

        val abstract = articleElement.ownerDocument().head().select("meta[property=og:description]").first()?.attr("content")?.trim() ?: ""

        var content = ""
        articleElement.select(".content").first()?.let { contentElement ->
            makeLinksAbsolute(contentElement, url)
            content = contentElement.outerHtml()

            articleElement.select("p > strong").forEach { // TODO: replace references to links in content with <a>
                if(it.text().trim() == "Links in diesem Artikel:") {
                    content += it.parent().outerHtml()
                }
            }
        }

        return EntryExtractionResult(Entry(content, abstract), reference)
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
package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.util.ItemExtractionResult
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class DerFreitagArticleExtractor(webClient: IWebClient) : ArticleExtractorBase(webClient) {

    companion object {
        private val log = LoggerFactory.getLogger(DerFreitagArticleExtractor::class.java)
    }

    
    override fun getName(): String? {
        return "Der Freitag"
    }

    override fun canExtractEntryFromUrl(url: String): Boolean {
        return isHttpOrHttpsUrlFromHost(url, "www.freitag.de/")
    }

    override fun parseHtmlToArticle(extractionResult: ItemExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { articleElement ->
            val articleEntry = Item(extractContent(articleElement, url))

            val reference = createReference(url, articleElement)
            
            extractionResult.setExtractedContent(articleEntry, reference)
        }
    }

    private fun extractContent(articleElement: Element, url: String): String {
        // or .x-article-text ?
        articleElement.select(".s-article-text").first()?.let { textElement ->
            removeSurveys(textElement)
            makeLinksAbsolute(textElement, url)
            loadLazyLoadingElements(textElement)
            adjustSourceElements(textElement)

            var content = articleElement.select(".abstract").first()?.outerHtml() ?: ""
            content += textElement.outerHtml()

            articleElement.select(".c-article-image--lead").first()?.let { previewImageElement ->
                if(previewImageElement.html().isNotBlank()) {
                    loadLazyLoadingElements(previewImageElement)
                    content = previewImageElement.outerHtml() + content
                }
            }

            return content
        }
        
        return ""
    }

    private fun removeSurveys(textElement: Element) {
        textElement.select("iframe").forEach { iframe ->
            if(iframe.attr("src").contains("://widget.civey.com/") || iframe.attr("data-src").contains("://widget.civey.com/")) {
                iframe.remove()
            }
        }
    }

    private fun extractImageGallery(imageGalleryElement: Element): String {
        var content = "<div>"
        content += imageGalleryElement.select("h2").outerHtml()

        content += extractAllImagesOfGallery(imageGalleryElement)

        return content + "</div>"
    }

    private fun extractAllImagesOfGallery(imageGalleryElement: Element): String {
        return ""
    }

    private fun createReference(articleUrl: String, articleElement: Element): Source {
        var title = ""

        articleElement.select("h1.title").first()?.let { titleElement ->
            title = titleElement.text()
        }

        val publishingDate = extractDate(articleElement)

        val articleReference = Source(title, articleUrl, publishingDate)
        articleReference.issue = tryToGetIssue(articleElement)

        tryToExtractPreviewImage(articleElement, articleReference)

        return articleReference
    }

    private fun tryToExtractPreviewImage(articleElement: Element, articleSource: Source) {
        articleElement.select(".c-article-image__media").first()?.let { previewImageElement ->
            articleSource.previewImageUrl = previewImageElement.attr("data-src")
        }
    }

    private fun tryToGetIssue(articleElement: Element): String? {
        articleElement.select(".issue").first()?.let { issueElement ->
            var issue = issueElement.text()

            if(issue != null && issue.contains("Ausgabe ")) { // remove 'Ausgabe ' and insert '/'
                issue = issue.replace("Ausgabe ", "")
                if(issue.length == 4) {
                    issue = issue.substring(0, 2) + "/" + issue.substring(2)
                }
            }

            return issue
        }

        return null
    }


    private var derFreitagDateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

    private fun extractDate(articleElement: Element): Date? {
        articleElement.select(".effective-date").first()?.let { dateElement ->
            var publishingDateString = dateElement.text().trim()

            try {
                val dateStartIndex = publishingDateString.indexOf(' ')
                publishingDateString = publishingDateString.substring(dateStartIndex).trim()

                return derFreitagDateFormat.parse(publishingDateString)
            } catch (e: Exception) {
                log.error("Could not parse Der Freitag DateTime Format " + publishingDateString, e)
            }
        }

        return null
    }


//    private fun addTags(bodyElement: Element, creationResult: EntryCreationResult) {
//        findOrCreateTagAndAddToCreationResult(creationResult)
//
//        val tagsElement = bodyElement.select("#article-keywords").first()
//        // TODO: may extract Article Tags
//    }

}

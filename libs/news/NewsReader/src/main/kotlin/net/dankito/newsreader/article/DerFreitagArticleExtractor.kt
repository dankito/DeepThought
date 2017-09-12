package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference
import net.dankito.deepthought.model.util.EntryExtractionResult
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
        return url.toLowerCase().contains("://www.freitag.de/") && url.length > "://www.freitag.de/".length + 5
    }

    override fun parseHtmlToArticle(extractionResult: EntryExtractionResult, document: Document, url: String) {
        document.body().select("article").first()?.let { articleElement ->
            val articleEntry = createEntry(articleElement, url)

            val reference = createReference(url, articleElement)
            
            extractionResult.setExtractedContent(articleEntry, reference)
        }
    }

    private fun createEntry(articleElement: Element, url: String): Entry {
        val abstractString = extractAbstract(articleElement)

        val content = extractContent(articleElement, url)

        return Entry(content, abstractString)
    }

    private fun extractAbstract(articleElement: Element): String {
        articleElement.select(".abstract").first()?.let { abstractElement ->
            return abstractElement.text()
        }

        return ""
    }

    private fun extractContent(articleElement: Element, url: String): String {
        // or .x-article-text ?
        articleElement.select(".s-article-text").first()?.let { textElement ->
            makeLinksAbsolute(textElement, url)
            loadLazyLoadingElements(textElement)
            adjustSourceElements(textElement)

            var content = textElement.outerHtml()

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

    private fun extractImageGallery(imageGalleryElement: Element): String {
        var content = "<div>"
        content += imageGalleryElement.select("h2").outerHtml()

        content += extractAllImagesOfGallery(imageGalleryElement)

        return content + "</div>"
    }

    private fun extractAllImagesOfGallery(imageGalleryElement: Element): String {
        return ""
    }

    private fun createReference(articleUrl: String, articleElement: Element): Reference {
        var title = ""

        articleElement.select("h1.title").first()?.let { titleElement ->
            title = titleElement.text()
        }

        val publishingDate = extractDate(articleElement)

        val articleReference = Reference(articleUrl, title, publishingDate)
        articleReference.issue = tryToGetIssue(articleElement)

        tryToExtractPreviewImage(articleElement, articleReference)

        return articleReference
    }

    private fun tryToExtractPreviewImage(articleElement: Element, articleReference: Reference) {
        articleElement.select(".c-article-image__media").first()?.let { previewImageElement ->
            articleReference.previewImageUrl = previewImageElement.attr("data-src")
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

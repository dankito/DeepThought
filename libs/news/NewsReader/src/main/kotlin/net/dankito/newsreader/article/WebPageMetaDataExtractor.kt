package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.util.EntryExtractionResult
import org.jsoup.nodes.Document


class WebPageMetaDataExtractor(webClient: IWebClient) : ExtractorBase(webClient) {

    fun extractMetaData(extractionResult: EntryExtractionResult, document: Document) {
        extractTitle(extractionResult, document)

        extractAbstract(extractionResult, document)

        extractSiteName(extractionResult, document)

        extractPublishingDate(extractionResult, document)

        extractPreviewImageUrl(extractionResult, document)

        // TODO: what about keywords (property=article:tag)?
    }

    private fun extractTitle(extractionResult: EntryExtractionResult, document: Document) {
        extractionResult.reference?.let { reference ->
            var title = document.head().select("meta[name=\"og:title\"]").first()?.attr("content") // html's title sometimes has website name in it, so try og:title first

            if(title == null) {
                title = document.title()
            }

            title?.let { reference.title = it }
        }
    }

    private fun extractAbstract(extractionResult: EntryExtractionResult, document: Document) {
        if(extractionResult.entry.abstractString.isBlank()) {
            var abstract = document.head().select("meta[name=\"description\"]").first()?.attr("content")

            if (abstract == null) {
                abstract = document.head().select("meta[name=\"og:description\"]").first()?.attr("content")
            }

            abstract?.let { extractionResult.entry.abstractString = it }
        }
    }

    private fun extractSiteName(extractionResult: EntryExtractionResult, document: Document) {
        extractionResult.reference?.let { reference ->
            if(reference.series == null) {
                var siteName = document.head().select("meta[name=\"publisher\"]").first()?.attr("content")

                if(siteName == null) {
                    siteName = document.head().select("meta[name=\"og:site_name\"]").first()?.attr("content")
                }

                siteName?.let { reference.series = Series(it) }
            }
        }
    }

    private fun extractPublishingDate(extractionResult: EntryExtractionResult, document: Document) {
        extractionResult.reference?.let { reference ->
            extractPublishingDateString(document)?.let { publishingDateString ->
                var publishingDate = parseIsoDateTimeString(publishingDateString)

                if(publishingDate == null) {
                    publishingDate = parseIsoDateTimeStringWithoutTimezone(publishingDateString)
                }

                reference.setPublishingDate(publishingDate, publishingDateString)
            }
        }
    }

    private fun extractPublishingDateString(document: Document): String? {
        var dateString = document.head().select("meta[name=\"last-modified\"]").first()?.attr("content")

        if(dateString == null) {
            dateString = document.head().select("meta[name=\"date\"]").first()?.attr("content")
        }

        if(dateString == null) {
            dateString = document.head().select("meta[name=\"pdate\"]").first()?.attr("content")
        }

        if(dateString == null) {
            dateString = document.head().select("meta[property=\"article:modified\"]").first()?.attr("content")
        }

        if(dateString == null) {
            dateString = document.head().select("meta[property=\"article:published\"]").first()?.attr("content")
        }

        if(dateString == null) {
            dateString = document.head().select("meta[property=\"article:modified_time\"]").first()?.attr("content")
        }

        if(dateString == null) {
            dateString = document.head().select("meta[property=\"article:published_time\"]").first()?.attr("content")
        }

        return dateString
    }

    private fun extractPreviewImageUrl(extractionResult: EntryExtractionResult, document: Document) {
        if(extractionResult.reference?.previewImageUrl == null) {
            extractionResult.reference?.previewImageUrl = document.head().select("meta[name=\"og:image\"]").first()?.attr("content")
        }
    }

}
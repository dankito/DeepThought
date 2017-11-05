package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import org.jsoup.nodes.Document
import java.util.*


class WebPageMetaDataExtractor(webClient: IWebClient) : ExtractorBase(webClient) {

    fun extractMetaData(document: Document): WebPageMetaData {
        val metaData = WebPageMetaData()

        metaData.title = extractTitle(document)

        metaData.description = extractDescription(document)

        metaData.siteName = extractSiteName(document)

        metaData.previewImageUrl = extractPreviewImageUrl(document)

        metaData.publishingDateString = extractPublishingDateString(document)
        metaData.publishingDateString?.let {
            metaData.publishingDate = tryToParsePublishingDateString(it)
        }

        // TODO: what about keywords (property=article:tag)?

        return metaData
    }

    private fun extractTitle(document: Document): String? {
        // html's title sometimes has website name in it, so try og:title first
        document.head().select("meta[name=\"og:title\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"og:title\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"title\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"fulltitle\"]").first()?.attr("content")?.let { return it }

        return document.title()
    }

    private fun extractDescription(document: Document): String? {
        document.head().select("meta[name=\"description\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"og:description\"]").first()?.attr("content")?.let { return it }

        return null
    }

    private fun extractSiteName(document: Document): String? {
        document.head().select("meta[name=\"publisher\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"og:site_name\"]").first()?.attr("content")?.let { return it }

        return null
    }

    private fun extractPublishingDateString(document: Document): String? {
        document.head().select("meta[name=\"last-modified\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"date\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"last-modified\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"pdate\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"article:modified\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"article:published\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"article:modified_time\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"article:published_time\"]").first()?.attr("content")?.let { return it }

        return null
    }

    private fun tryToParsePublishingDateString(publishingDateString: String): Date? {
        parseIsoDateTimeString(publishingDateString)?.let { return it }

        parseIsoDateTimeStringWithoutTimezone(publishingDateString)?.let { return it }

        parseVeryDetailedDateTimeString(publishingDateString)?.let { return it }

        return null
    }

    private fun extractPreviewImageUrl(document: Document): String? {
        document.head().select("meta[name=\"og:image\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"og:image\"]").first()?.attr("content")?.let { return it }

        return null
    }

}
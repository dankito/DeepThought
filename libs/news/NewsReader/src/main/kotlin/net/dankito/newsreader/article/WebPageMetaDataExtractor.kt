package net.dankito.newsreader.article

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import org.jsoup.nodes.Document
import java.util.*
import kotlin.collections.HashSet


class WebPageMetaDataExtractor(webClient: IWebClient) : ExtractorBase(webClient) {

    fun extractMetaData(document: Document): WebPageMetaData {
        val metaData = WebPageMetaData()

        metaData.title = extractAndRemoveSiteNameFromTitle(document)

        metaData.description = extractDescription(document)

        metaData.siteName = extractSiteName(document)

        metaData.previewImageUrl = extractPreviewImageUrl(document)

        metaData.author = extractAuthor(document)

        metaData.keywords = extractKeywords(document)

        metaData.publishingDateString = extractPublishingDateString(document)
        metaData.publishingDateString?.let {
            metaData.publishingDate = tryToParsePublishingDateString(it)
        }

        return metaData
    }

    private fun extractAndRemoveSiteNameFromTitle(document: Document): String? {
        val extractedTitle = extractTitle(document)

        extractedTitle?.let {
            return removeSiteNameFromTitle(it)
        }

        return extractedTitle
    }

    private fun extractTitle(document: Document): String? {
        // html's title sometimes has website name in it, so try og:title first
        document.head().select("meta[name=\"og:title\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"og:title\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"title\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"fulltitle\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[itemprop=\"alternativeHeadline\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"hdl\"]").first()?.attr("content")?.let { return it }

        return document.title()
    }

    private fun removeSiteNameFromTitle(extractedTitle: String): String {
        var adjustedTitle = extractedTitle

        if(extractedTitle.contains(" [\\|\\-\\/>»] ".toRegex())) {
            adjustedTitle = extractedTitle.replace("(.*)[\\|\\-\\/>»] .*".toRegex(RegexOption.IGNORE_CASE), "$1")

            // If the resulting title is too short (3 words or fewer), remove
            // the first part instead:
            if(wordCount(adjustedTitle) < 3) {
                adjustedTitle = adjustedTitle.replace("[^\\|\\-\\/>»]*[\\|\\-\\/>»](.*)".toRegex(RegexOption.IGNORE_CASE), "$1")
            }

            return adjustedTitle
        }
        else if(extractedTitle.contains(": ")) {
            adjustedTitle = adjustedTitle.substring(adjustedTitle.lastIndexOf(':') + 1)

            // If the title is now too short, try the first colon instead:
            if(wordCount(adjustedTitle) < 3) {
                adjustedTitle = adjustedTitle.substring(adjustedTitle.indexOf(':') + 1)
            }
        }

        return adjustedTitle
    }

    private fun wordCount(string: String): Int {
        return string.split("\\s+".toRegex()).size
    }


    private fun extractDescription(document: Document): String? {
        document.head().select("meta[name=\"description\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"og:description\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"og:description\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"DC.description\"]").first()?.attr("content")?.let { return it }

        return null
    }

    private fun extractSiteName(document: Document): String? {
        document.head().select("meta[name=\"publisher\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"og:site_name\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"og:site_name\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"application-name\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"twitter:site\"]").first()?.attr("content")?.let {
            if(it.startsWith('@')) {
                return it.substring(1)
            }
            return it
        }

        document.head().select("meta[name=\"apple-mobile-web-app-title\"]").first()?.attr("content")?.let { return it }

        return null
    }

    private fun extractAuthor(document: Document): String? {
        // TODO: there can be multiple author elements
        document.head().select("meta[name=\"author\"]").first()?.attr("content")?.let { return it }

        // TODO: there can be multiple article:author elements
        document.head().select("meta[property=\"article:author\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"publisher\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"twitter:app:name:googleplay\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"twitter:app:name:iphone\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"twitter:app:name:ipad\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"al:android:app_name\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"al:iphone:app_name\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[property=\"al:ipad:app_name\"]").first()?.attr("content")?.let { return it }

        document.head().select("meta[name=\"copyright\"]").first()?.attr("content")?.let { return it }

        return null
    }

    private fun extractKeywords(document: Document): MutableSet<String> {
        val keywords = HashSet<String>()

        document.head().select("meta[name=\"keywords\"]").first()?.attr("content")?.let {
            extractKeywords(it, keywords)
        }

        document.head().select("meta[name=\"news_keywords\"]").first()?.attr("content")?.let {
            extractKeywords(it, keywords)
        }

        document.head().select("meta[property=\"article:tag\"]").first()?.attr("content")?.let {
            extractKeywords(it, keywords)
        }

        return keywords
    }

    private fun extractKeywords(keywordsString: String, keywords: MutableSet<String>) {
        keywordsString.split(",").map { it.trim() }.forEach {
            keywords.add(it)
        }
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

        document.head().select("meta[name=\"DC.date.issued\"]").first()?.attr("content")?.let { return it }

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

        document.head().select("meta[property=\"twitter:image\"]").first()?.attr("content")?.let { return it }

        document.head().select("link[rel=\"image_src\"]").first()?.attr("href")?.let { return it }

        document.head().select("meta[name=\"thumbnail\"]").first()?.attr("content")?.let { return it } // TODO: really try thumbnail?

        return null
    }

}
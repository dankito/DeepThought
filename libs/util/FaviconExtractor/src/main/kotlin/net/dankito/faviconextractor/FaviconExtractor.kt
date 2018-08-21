package net.dankito.faviconextractor

import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.RequestParameters
import net.dankito.utils.web.client.ResponseType
import net.dankito.utils.web.client.WebClientResponse
import net.dankito.utils.AsyncResult
import net.dankito.data_access.network.webclient.extractor.ExtractorBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.concurrent.thread


class FaviconExtractor(webClient : IWebClient) : ExtractorBase(webClient) {

    companion object {
        private val log = LoggerFactory.getLogger(FaviconExtractor::class.java)
    }


    fun extractFaviconsAsync(url: String, callback: (AsyncResult<List<Favicon>>) -> Unit) {
        thread {
            try {
                callback(AsyncResult(true, result = extractFavicons(url)))
            } catch(e: Exception) {
                log.error("Could not get favicons for " + url, e)
                callback(AsyncResult(false, e))
            }
        }
    }

    private fun extractFavicons(url: String) : List<Favicon> {
        webClient.get(RequestParameters(url)).let { response ->
            if(response.isSuccessful) {
                return extractFavicons(response, url)
            }
        }

        return listOf()
    }

    private fun extractFavicons(response: WebClientResponse, url: String): List<Favicon> {
        val document = Jsoup.parse(response.body, url)

        return extractFavicons(document, url)
    }

    fun extractFavicons(document: Document, url: String): List<Favicon> {
        val extractedFavicons = document.head().select("link, meta").map { mapElementToFavicon(it, url) }.filterNotNull().toMutableList()

        tryToFindDefaultFavicon(url, extractedFavicons)

        return extractedFavicons
    }

    private fun tryToFindDefaultFavicon(url: String, extractedFavicons: MutableList<Favicon>) {
        val urlInstance = URL(url)
        val defaultFaviconUrl = urlInstance.protocol + "://" + urlInstance.host + "/favicon.ico"
        webClient.get(RequestParameters(defaultFaviconUrl, responseType = ResponseType.Bytes)).let { response ->
            if(response.isSuccessful && containsIconWithUrl(extractedFavicons, defaultFaviconUrl) == false) {
                extractedFavicons.add(Favicon(defaultFaviconUrl, FaviconType.ShortcutIcon))
            }
        }
    }

    private fun containsIconWithUrl(extractedFavicons: MutableList<Favicon>, faviconUrl: String): Boolean {
        extractedFavicons.forEach {
            if(it.url == faviconUrl) {
                return true
            }
        }

        return false
    }

    /**
     * Possible formats are documented here https://stackoverflow.com/questions/21991044/how-to-get-high-resolution-website-logo-favicon-for-a-given-url#answer-22007642
     * and here https://en.wikipedia.org/wiki/Favicon
     */
    private fun mapElementToFavicon(linkOrMetaElement: Element, siteUrl: String): Favicon? {
        if(linkOrMetaElement.nodeName() == "link") {
            return mapLinkElementToFavicon(linkOrMetaElement, siteUrl)
        }
        else if(linkOrMetaElement.nodeName() == "meta") {
            return mapMetaElementToFavicon(linkOrMetaElement, siteUrl)
        }

        return null
    }

    private fun mapLinkElementToFavicon(linkElement: Element, siteUrl: String): Favicon? {
        if(linkElement.hasAttr("rel")) {
            val relValue = linkElement.attr("rel")

            if(relValue == "icon") {
                return createFavicon(linkElement.attr("href"), siteUrl, FaviconType.Icon, linkElement.attr("sizes"), linkElement.attr("type"))
            }
            else if(relValue.startsWith("apple-touch-icon")) {
                val iconType = if(relValue.endsWith("-precomposed")) FaviconType.AppleTouchPrecomposed else FaviconType.AppleTouch
                return createFavicon(linkElement.attr("href"), siteUrl, iconType, linkElement.attr("sizes"), linkElement.attr("type"))
            }
            else if(relValue == "shortcut icon") {
                return createFavicon(linkElement.attr("href"), siteUrl, FaviconType.ShortcutIcon, linkElement.attr("sizes"), linkElement.attr("type"))
            }
        }

        return null
    }

    private fun mapMetaElementToFavicon(metaElement: Element, siteUrl: String): Favicon? {
        if(isOpenGraphImageDeclaration(metaElement)) {
            return Favicon(makeLinkAbsolute(metaElement.attr("content"), siteUrl), FaviconType.OpenGraphImage)
        }
        else if(isMsTileMetaElement(metaElement)) {
            return Favicon(makeLinkAbsolute(metaElement.attr("content"), siteUrl), FaviconType.MsTileImage)
        }

        return null
    }

    private fun isOpenGraphImageDeclaration(metaElement: Element) = metaElement.hasAttr("property") && metaElement.attr("property") == "og:image" && metaElement.hasAttr("content")

    private fun isMsTileMetaElement(metaElement: Element) = metaElement.hasAttr("name") && metaElement.attr("name") == "msapplication-TileImage" && metaElement.hasAttr("content")


    private fun createFavicon(url: String?, siteUrl: String, iconType: FaviconType, sizesString: String?, type: String?): Favicon? {
        if(url != null) {
            val favicon = Favicon(makeLinkAbsolute(url, siteUrl), iconType, type = type)

            if (sizesString != null) {
                val sizes = extractSizesFromString(sizesString)
                if(sizes.isNotEmpty()) {
                    favicon.size = sizes.sortedDescending().first()
                }
            }

            return favicon
        }

        return null
    }

    private fun extractSizesFromString(sizesString: String): List<Size> {
        val sizes = sizesString.split(" ").map { sizeString -> mapSizeString(sizeString) }.filterNotNull()

        return sizes
    }

    private fun mapSizeString(sizeString: String) : Size? {
        var parts = sizeString.split('x')
        if(parts.size != 2) {
            parts = sizeString.split('×') // actually doesn't meet specification, see https://www.w3schools.com/tags/att_link_sizes.asp, but New York Times uses it
        }
        if(parts.size != 2) {
            parts = sizeString.split('X')
        }

        if (parts.size == 2) {
            val width = parts[0].toIntOrNull()
            val height = parts[1].toIntOrNull()

            if (width != null && height != null) {
                return Size(width, height)
            }
        }

        return null
    }

}
package net.dankito.utils.services.network

import net.dankito.utils.web.UrlUtil
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.RequestParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


abstract class ExtractorBase(val webClient : IWebClient, protected val urlUtil: UrlUtil = UrlUtil()) {

    companion object {
        protected val isoDateTimeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        protected val isoDateTimeFormatWithoutTimezone: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        protected val detailedDateTimeFormat: DateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")

        const val DesktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
        const val MobileUserAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"
    }


    protected fun requestUrl(url: String): Document {
        val parameters = createParametersForUrl(url)

        webClient.get(parameters).let { response ->
            if(response.isSuccessful) {
                return Jsoup.parse(response.body, url)
            }
            else {
                throw Exception(response.error)
            }
        }
    }

    protected fun requestUrlWithPost(url: String, body: String? = null, contentType: String? = null): Document {
        val parameters = createParametersForUrl(url, body)
        parameters.contentType = contentType

        webClient.post(parameters).let { response ->
            if(response.isSuccessful) {
                return Jsoup.parse(response.body, url)
            }
            else {
                throw Exception(response.error)
            }
        }
    }

    protected open fun createParametersForUrl(url: String, body: String? = null): RequestParameters {
        val parameters = RequestParameters(url, body, userAgent = MobileUserAgent)

        return parameters
    }


    protected open fun makeLinkAbsolute(url: String, siteUrl: String): String {
        return urlUtil.makeLinkAbsolute(url, siteUrl)
    }


    protected open fun loadLazyLoadingElements(element: Element) {
        for (lazyLoadingElement in element.select("[data-src]")) {
            loadLazyLoadingElement(lazyLoadingElement, "data-src", "src")
        }

        for (lazyLoadingElement in element.select("[data-srcset]")) {
            loadLazyLoadingElement(lazyLoadingElement, "data-srcset", "srcset")
        }
    }

    protected open fun loadLazyLoadingElement(lazyLoadingElement: Element): Element {
        return loadLazyLoadingElement(lazyLoadingElement, "data-src", "src")
    }

    protected open fun loadLazyLoadingElement(lazyLoadingElement: Element, lazyLoadingAttributeName: String, destinationAttributeName: String): Element {
        val source = lazyLoadingElement.attr(lazyLoadingAttributeName)

        if (source.isNotBlank()) {
            lazyLoadingElement.attr(destinationAttributeName, source)
        }

        return lazyLoadingElement
    }


    protected fun getLazyLoadingOrNormalUrlAndMakeLinkAbsolute(element: Element, attributeName: String, siteUrl: String): String {
        val source = element.attr("data-src")

        if(source.isNotBlank()) { // if element does not have attribute 'data-src' a blank string is returned
            return makeLinkAbsolute(source, siteUrl)
        }

        return makeLinkAbsolute(element.attr(attributeName), siteUrl)
    }


    protected fun parseIsoDateTimeString(isoDateTimeString: String): Date? {
        var editableIsoDateTimeString = isoDateTimeString

        if(editableIsoDateTimeString.length > 18 && ':' == editableIsoDateTimeString[editableIsoDateTimeString.length - 3]) { // remove colon from time zone, Java DateFormat is  not able to parse it
            editableIsoDateTimeString = editableIsoDateTimeString.substring(0, editableIsoDateTimeString.length - 3) + editableIsoDateTimeString.substring(editableIsoDateTimeString.length - 2)
        }

        try {
            return isoDateTimeFormat.parse(editableIsoDateTimeString)
        } catch (e: Exception) { }

        return null
    }

    protected fun parseIsoDateTimeStringWithoutTimezone(isoDateTimeString: String): Date? {
        try {
            return isoDateTimeFormatWithoutTimezone.parse(isoDateTimeString)
        } catch (e: Exception) { }

        return null
    }

    /**
     * Parses a date time string like Mo, 30 Okt 2017 12:36:45 MEZ
     */
    protected fun parseVeryDetailedDateTimeString(detailedDateTimeString: String): Date? {
        try {
            return detailedDateTimeFormat.parse(detailedDateTimeString)
        } catch (e: Exception) { }

        return null
    }


    protected fun convertNonBreakableSpans(text: String): String {
        return text.replace("\u00A0", " ") // Converting &nbsp; entities
    }

    protected fun convertGuardedAreaToDash(text: String): String {
        return text.replace('\u0096', '-')
    }

}
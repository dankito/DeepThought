package net.dankito.data_access.network.webclient.extractor

import net.dankito.util.web.IWebClient
import net.dankito.util.web.RequestParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


// TODO: find a better library
abstract class ExtractorBase(val webClient : IWebClient) {

    companion object {
        protected val isoDateTimeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        protected val isoDateTimeFormatWithoutTimezone: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        protected val detailedDateTimeFormat: DateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
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

    protected fun requestUrlWithPost(url: String, body: String? = null): Document {
        val parameters = createParametersForUrl(url, body)

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
        val parameters = RequestParameters(url, body, userAgent = RequestParameters.DEFAULT_MOBILE_USER_AGENT)

        return parameters
    }


    protected open fun makeLinkAbsolute(url: String, siteUrl: String): String {
        var absoluteUrl = url

        if(url.startsWith("//")) {
            if(siteUrl.startsWith("https:")) {
                absoluteUrl = "https:" + url
            }
            else {
                absoluteUrl = "http:" + url
            }
        }
        else if(url.startsWith("/")) {
            tryToMakeUrlAbsolute(url, siteUrl)?.let { absoluteUrl = it }
        }
        else if(url.startsWith("http") == false) {
            tryToMakeUrlAbsolute(url, siteUrl)?.let { absoluteUrl = it }
        }

        return absoluteUrl
    }

    private fun tryToMakeUrlAbsolute(relativeUrl: String, siteUrl: String): String? {
        try {
            val relativeUri = URI(relativeUrl)
            if(relativeUri.isAbsolute && relativeUri.scheme.startsWith("http") == false) {
                return relativeUrl // it's an absolute uri but just doesn't start with http, e.g. mailto: for file:
            }
        } catch(ignored: Exception) { }

        try {
            val uri = URI(siteUrl)
            return uri.resolve(relativeUrl).toString()
        } catch(ignored: Exception) { }

        try {
            val uri = URI(siteUrl)

            val port = if(uri.port > 0) ":" + uri.port else ""
            val separator = if(relativeUrl.startsWith("/")) "" else "/"

            val manuallyCreatedUriString = uri.scheme + "://" + uri.host + port + separator + relativeUrl
            val manuallyCreatedUri = URI(manuallyCreatedUriString)
            return manuallyCreatedUri.toString()
        } catch(ignored: Exception) { }

        return null
    }


    protected open fun loadLazyLoadingElements(element: Element) {
        for (lazyLoadingElement in element.select("[data-src]")) {
            loadLazyLoadingElement(lazyLoadingElement)
        }
    }

    protected open fun loadLazyLoadingElement(lazyLoadingElement: Element): Element {
        val source = lazyLoadingElement.attr("data-src")

        if(source.isNotBlank()) {
            when (lazyLoadingElement.nodeName()) {
                "img" -> lazyLoadingElement.attr("src", source)
                else -> lazyLoadingElement.attr("src", source)
            }
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
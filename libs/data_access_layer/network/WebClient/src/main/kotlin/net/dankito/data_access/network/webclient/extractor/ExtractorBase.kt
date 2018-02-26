package net.dankito.data_access.network.webclient.extractor

import net.dankito.util.HtmlUtil
import net.dankito.util.web.IWebClient
import net.dankito.util.web.RequestParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


// TODO: find a better library
abstract class ExtractorBase(val webClient : IWebClient): HtmlUtil() {

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

}
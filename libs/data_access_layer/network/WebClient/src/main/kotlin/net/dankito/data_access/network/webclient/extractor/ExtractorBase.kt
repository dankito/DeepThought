package net.dankito.data_access.network.webclient.extractor

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.RequestParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI


// TODO: find a better library
abstract class ExtractorBase(val webClient : IWebClient) {


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

    protected open fun createParametersForUrl(url: String): RequestParameters {
        val parameters = RequestParameters(url)

        return parameters
    }


    protected open fun makeLinkAbsolute(url: String, siteUrl: String): String {
        var absoluteUrl = url

        if(url.startsWith("//")) {
            if(siteUrl.startsWith("https:")) {
                absoluteUrl = "https:" + url
            }
            else {
                absoluteUrl = "http" + url
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

            val uri = URI(siteUrl)
            return uri.resolve(relativeUrl).toString()
//            val port = if(uri.port > 0) ":" + uri.port else ""
//            val separator = if(relativeUrl.startsWith("/")) "" else "/"
//            return uri.scheme + "://" + uri.host + port + separator + relativeUrl
        } catch(ignored: Exception) { }

        return null
    }

}
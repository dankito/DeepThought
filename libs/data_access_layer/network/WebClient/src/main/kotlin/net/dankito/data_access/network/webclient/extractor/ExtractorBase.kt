package net.dankito.data_access.network.webclient.extractor

import net.dankito.data_access.network.webclient.IWebClient
import net.dankito.data_access.network.webclient.RequestParameters
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL


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
            val urlInstance = URL(URL(siteUrl), url)
            absoluteUrl = urlInstance.toExternalForm()
        }
        else if(url.startsWith("http") == false) {
            val urlInstance = URL(URL(siteUrl), url)
            absoluteUrl = urlInstance.toExternalForm()
        }

        return absoluteUrl
    }

}
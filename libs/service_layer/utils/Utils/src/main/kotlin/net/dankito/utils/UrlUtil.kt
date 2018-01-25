package net.dankito.utils

import java.net.URI


class UrlUtil {

    fun isUri(string: String): Boolean {
        try {
            val uri = URI.create(string)
            return uri != null && uri.scheme != null
        } catch(ignored: Exception) { } // ok, sharedText is not an Uri

        return false
    }

    /**
     * Returns true if parameter is a valid uri and scheme is either 'http' or 'https'.
     */
    fun isHttpUri(string: String): Boolean {
        try {
            val uri = URI.create(string)
            return uri != null && (uri.scheme == "http" || uri.scheme == "https")
        } catch(ignored: Exception) { } // ok, sharedText is not an Uri

        return false
    }


    fun getHostName(url: String): String? {
        try {
            val uri = URI.create(url)
            var host = uri.host

            if(host.startsWith("www.")) {
                host = host.substring(4)
            }

            return host
        } catch(e: Exception) { }

        return "URL"
    }

}
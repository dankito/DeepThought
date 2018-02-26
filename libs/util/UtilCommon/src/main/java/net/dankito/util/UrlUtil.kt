package net.dankito.util

import java.net.URI


open class UrlUtil {

    open fun isUri(string: String): Boolean {
        try {
            val uri = URI.create(string)
            return uri != null && uri.scheme != null
        } catch(ignored: Exception) { } // ok, sharedText is not an Uri

        return false
    }

    /**
     * Returns true if parameter is a valid uri and scheme is either 'http' or 'https'.
     */
    open fun isHttpUri(string: String): Boolean {
        try {
            val uri = URI.create(string)
            return uri != null && (uri.scheme == "http" || uri.scheme == "https")
        } catch(ignored: Exception) { } // ok, sharedText is not an Uri

        return false
    }


    open fun getHostName(url: String): String? {
        var host = url.substringAfter("://").substringBefore('/') // as fallback if parsing URI doesn't work

        try {
            val uri = URI.create(url)
            host = uri.host
        } catch(e: Exception) { }


        host = tryToRemoveDomainUrlAndWWW(host)

        return host
    }

    protected open fun tryToRemoveDomainUrlAndWWW(host: String): String {
        try {
            val lastIndexOfDot = host.lastIndexOf('.')

            if(lastIndexOfDot > 0) {
                var nextIndexOfDot = host.lastIndexOf('.', lastIndexOfDot - 1)

                if(nextIndexOfDot >= lastIndexOfDot - 4) { // e.g. domains like .co.uk, ...
                    nextIndexOfDot = host.lastIndexOf('.', nextIndexOfDot - 1)
                }

                if(nextIndexOfDot > -1) {
                    return host.substring(nextIndexOfDot + 1)
                }
            }
        } catch(e: Exception) { }

        return host
    }


    open fun getFileName(url: String): String {
        try {
            val uri = URI(url)
            val path = uri.path

            return path.substringAfterLast('/')
        } catch(e: Exception) { }

        return url.substringAfterLast('/').substringBefore('?')
    }

}
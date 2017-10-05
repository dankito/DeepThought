package net.dankito.utils

import java.net.URI


class UrlUtil {

    fun isUri(string: String): Boolean {
        try {
            val uri = URI.create(string)
            return uri != null
        } catch(ignored: Exception) { } // ok, sharedText is not an Uri

        return false
    }

}
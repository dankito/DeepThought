package net.dankito.data_access.network.webclient

import java.io.InputStream


data class WebClientResponse(val isSuccessful: Boolean,
                             val headers: Map<String, String>? = null,
                             val error: Exception? = null,
                             val body: String? = null, val responseStream: InputStream? = null) {

    fun getHeaderValue(headerName: String): String? {
        val headerNameLowerCased = headerName.toLowerCase() // header names are case insensitive, so compare them lower cased

        headers?.keys?.forEach {
            if(it.toLowerCase() == headerNameLowerCased) {
                return headers[it]
            }
        }

        return null
    }

}
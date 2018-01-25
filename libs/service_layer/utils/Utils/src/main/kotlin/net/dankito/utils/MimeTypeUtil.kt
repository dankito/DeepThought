package net.dankito.utils

import org.slf4j.LoggerFactory
import java.net.URLConnection


class MimeTypeUtil {

    companion object {
        const val HtmlMimeType = "text/html"

        private val log = LoggerFactory.getLogger(MimeTypeUtil::class.java)
    }


    fun getMimeTypeForName(nameOrUrl: String): String? {
        try {
            return URLConnection.guessContentTypeFromName(nameOrUrl)
        } catch(e: Exception) {
            log.info("Could not guess Mime type from name or url $nameOrUrl", e)
        }

        return null
    }


    fun isHttpUrlAWebPage(httpUrl: String): Boolean {
        val mimeType = getMimeTypeForName(httpUrl)

        return mimeType == HtmlMimeType || mimeType == null // if it's null then it's a least not a known mime type, e.g. not a Pdf, image etc., but foreseeable a web page without file extension
    }

}
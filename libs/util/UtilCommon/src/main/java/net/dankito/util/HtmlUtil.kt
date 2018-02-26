package net.dankito.util

import java.net.URI
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


open class HtmlUtil {

    companion object {
        protected val isoDateTimeFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        protected val isoDateTimeFormatWithoutTimezone: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        protected val detailedDateTimeFormat: DateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
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
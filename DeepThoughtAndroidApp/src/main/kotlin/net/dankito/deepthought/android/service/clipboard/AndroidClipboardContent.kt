package net.dankito.deepthought.android.service.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import net.dankito.deepthought.service.clipboard.ClipboardContent
import net.dankito.utils.UrlUtil


class AndroidClipboardContent(private val item: ClipData.Item, private val description: ClipDescription, private val urlUtil: UrlUtil) : ClipboardContent() {


    override fun hasPlainText() = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)

    override val plainText = item.text?.toString()


    override fun hasUrl(): Boolean {
        // urls are mostly copied as plain text to clipboard, not as Android Uri
        return description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST) ||
                (hasPlainText() && plainText != null && urlUtil.isHttpUri(plainText.trim()))
    }

    override val url: String?
        get() {
            if(description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST)) {
                return item.uri?.toString()
            }
            else if(plainText != null && urlUtil.isHttpUri(plainText.trim())) {
                return plainText.trim()
            }

            return null
        }


    override fun hasHtml(): Boolean {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        }

        return false
    }

    override val html: String?
        get() {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                return item.htmlText
            }

            return item.text?.toString()
        }


    // Android Clipboard can only contain plain text, html, an uri or an intent

    override fun hasRtf() = false

    override val rtf = null


    override fun hasImage() = false

    override val image = null

    override fun hasFiles() = false

    override val files = null
}
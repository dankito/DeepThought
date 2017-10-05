package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import net.dankito.utils.UrlUtil
import java.io.File


class JavaFXClipboardContent(private val clipboard: Clipboard, private val urlUtil: UrlUtil) {


    fun hasPlainText(): Boolean {
        return clipboard.hasString()
    }

    val plainText: String? = clipboard.string


    fun hasUrl(): Boolean {
        val text = this.plainText
        return clipboard.hasUrl() || (text != null && urlUtil.isUri(text))
    }

    val url: String?
        get() {
            clipboard.url?.let { return it }

            plainText?.let {
                if(urlUtil.isUri(it)) {
                    return it
                }
            }

            return null
        }


    fun hasHtml(): Boolean {
        return clipboard.hasHtml()
    }

    val html: String? = clipboard.html


    fun hasRtf(): Boolean {
        return clipboard.hasRtf()
    }

    val rtf: String? = clipboard.rtf


    fun hasImage(): Boolean {
        return clipboard.hasImage()
    }

    val image: Image? = clipboard.image


    fun hasFiles(): Boolean {
        return clipboard.hasFiles()
    }

    val files: List<File>? = clipboard.files

}

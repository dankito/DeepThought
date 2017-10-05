package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import net.dankito.utils.UrlUtil
import java.io.File


class JavaFXClipboardContent(private val clipboard: Clipboard, private val urlUtil: UrlUtil) {


    fun hasString(): Boolean {
        return clipboard.hasString()
    }

    val string: String?
        get() = clipboard.string

    fun hasUrl(): Boolean {
        val text = this.string
        return clipboard.hasUrl() || (text != null && urlUtil.isUri(text))
    }

    val url: String?
        get() {
            string?.let {
                if(urlUtil.isUri(it)) {
                    return it
                }
            }

            return clipboard.url
        }

    fun hasHtml(): Boolean {
        return clipboard.hasHtml()
    }

    val html: String?
        get() = clipboard.html

    fun hasRtf(): Boolean {
        return clipboard.hasRtf()
    }

    val rtf: String?
        get() = clipboard.rtf

    fun hasImage(): Boolean {
        return clipboard.hasImage()
    }

    val image: Image?
        get() = clipboard.image

    fun hasFiles(): Boolean {
        return clipboard.hasFiles()
    }

    val files: List<File>?
        get() = clipboard.files

}

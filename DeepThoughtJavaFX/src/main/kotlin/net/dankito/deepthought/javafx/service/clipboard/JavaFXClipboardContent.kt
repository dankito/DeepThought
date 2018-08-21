package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.input.Clipboard
import net.dankito.deepthought.javafx.ui.JavaFXImage
import net.dankito.deepthought.service.clipboard.ClipboardContent
import net.dankito.deepthought.ui.Image
import net.dankito.utils.web.UrlUtil
import java.io.File


class JavaFXClipboardContent(private val clipboard: Clipboard, private val urlUtil: UrlUtil) : ClipboardContent() {


    override fun hasPlainText(): Boolean {
        return clipboard.hasString()
    }

    override val plainText: String? = clipboard.string


    override fun hasUrl(): Boolean {
        val text = this.plainText
        return clipboard.hasUrl() || (text != null && urlUtil.isHttpUri(text))
    }

    override val url: String?
        get() {
            clipboard.url?.let { return it }

            plainText?.let {
                if(urlUtil.isHttpUri(it)) {
                    return it
                }
            }

            return null
        }


    override fun hasHtml(): Boolean {
        return clipboard.hasHtml()
    }

    override val html: String? = clipboard.html


    override fun hasRtf(): Boolean {
        return clipboard.hasRtf()
    }

    override val rtf: String? = clipboard.rtf


    override fun hasImage(): Boolean {
        return clipboard.hasImage()
    }

    override val image: Image?
        get() {
            clipboard.image?.let {
                return JavaFXImage(clipboard.image)
            }

            return null
        }


    override fun hasFiles(): Boolean {
        return clipboard.hasFiles()
    }

    override val files: List<File>? = clipboard.files

}

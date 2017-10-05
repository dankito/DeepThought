package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import java.io.File


class JavaFXClipboardContent(private val clipboard: Clipboard) {


    fun hasString(): Boolean {
        return clipboard.hasString()
    }

    val string: String?
        get() = clipboard.string

    fun hasUrl(): Boolean {
        return clipboard.hasUrl()
    }

    val url: String?
        get() = clipboard.url

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

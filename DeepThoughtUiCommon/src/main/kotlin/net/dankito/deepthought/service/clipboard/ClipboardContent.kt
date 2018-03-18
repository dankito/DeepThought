package net.dankito.deepthought.service.clipboard

import net.dankito.deepthought.ui.Image
import java.io.File


abstract class ClipboardContent {


    abstract fun hasPlainText(): Boolean

    abstract val plainText: String?


    abstract fun hasUrl(): Boolean

    abstract val url: String?


    abstract fun hasHtml(): Boolean

    abstract val html: String?


    abstract fun hasRtf(): Boolean

    abstract val rtf: String?


    abstract fun hasImage(): Boolean

    abstract val image: Image?


    abstract fun hasFiles(): Boolean

    abstract val files: List<File>?

}

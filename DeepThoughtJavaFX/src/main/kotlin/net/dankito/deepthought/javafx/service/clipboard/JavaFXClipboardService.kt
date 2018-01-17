package net.dankito.deepthought.javafx.service.clipboard

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.utils.ui.ClipboardServiceBase




class JavaFXClipboardService : ClipboardServiceBase() {

    override fun copyUrlToClipboard(url: String) {
        val content = ClipboardContent()

        content.putString(url) // also copy URL as Plain Text
        content.putUrl(url)

        Clipboard.getSystemClipboard().setContent(content)
    }

    override fun copyEntryToClipboard(item: Item, tags: Collection<Tag>, source: Source?, series: Series?) {
        val content = ClipboardContent()

        content.putString(convertItemToStringForCopyingToClipboard(item, tags, source, series))

        Clipboard.getSystemClipboard().setContent(content)
    }

}
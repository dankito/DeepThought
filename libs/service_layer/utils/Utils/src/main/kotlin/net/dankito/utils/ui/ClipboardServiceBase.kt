package net.dankito.utils.ui

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate


abstract class ClipboardServiceBase : IClipboardService {

    protected fun convertItemToStringForCopyingToClipboard(item: Item, tags: Collection<Tag>, source: Source?, series: Series?): String {
        var itemString = item.contentPlainText

        if(tags.isNotEmpty()) {
            itemString += "$itemString\n\n${tags.joinToString { it.name }}"
        }

        if(source != null) {
            itemString = "$itemString\n\n(${source.getPreviewWithSeriesAndPublishingDate(series)}: ${source.url})"
        }

        return itemString
    }

}
package net.dankito.utils.ui

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.getPreviewWithSeriesAndPublishingDate


abstract class ClipboardServiceBase : IClipboardService {

    protected fun convertItemToStringForCopyingToClipboard(item: Item, source: Source?, series: Series?): String {
        var itemString = item.contentPlainText

        if(source != null) {
            itemString = "$itemString\n\n(${source.getPreviewWithSeriesAndPublishingDate(series)}: ${source.url})"
        }

        return itemString
    }

}
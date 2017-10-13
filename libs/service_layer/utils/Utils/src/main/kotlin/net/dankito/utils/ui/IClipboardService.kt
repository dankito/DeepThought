package net.dankito.utils.ui

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source


interface IClipboardService {

    fun copyUrlToClipboard(url: String)

    fun copyEntryToClipboard(item: Item, source: Source?, series: Series?)

}
package net.dankito.utils.ui

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag


interface IClipboardService {

    fun copyUrlToClipboard(url: String)

    fun copyEntryToClipboard(item: Item, tags: Collection<Tag>, source: Source?, series: Series?)

}
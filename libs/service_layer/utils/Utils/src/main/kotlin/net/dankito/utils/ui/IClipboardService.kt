package net.dankito.utils.ui

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Reference


interface IClipboardService {

    fun copyUrlToClipboard(url: String)

    fun copyEntryToClipboard(entry: Entry, reference: Reference?)

}
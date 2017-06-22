package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag


interface IEntriesListView {

    fun showEntries(entries: List<Entry>)

    fun showEntriesForTag(tag: Tag, entries: List<Entry>)

}
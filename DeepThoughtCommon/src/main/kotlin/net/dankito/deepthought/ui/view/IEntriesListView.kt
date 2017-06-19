package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Entry


interface IEntriesListView {

    fun showEntries(entries: List<Entry>)

}
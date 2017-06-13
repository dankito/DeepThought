package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Entry


interface IMainView {

    fun showEntries(entries: List<Entry>)

}
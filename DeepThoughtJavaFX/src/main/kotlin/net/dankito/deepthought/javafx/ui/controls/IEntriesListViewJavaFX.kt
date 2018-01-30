package net.dankito.deepthought.javafx.ui.controls

import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.view.IEntriesListView


interface IEntriesListViewJavaFX : IEntriesListView {

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>)

}
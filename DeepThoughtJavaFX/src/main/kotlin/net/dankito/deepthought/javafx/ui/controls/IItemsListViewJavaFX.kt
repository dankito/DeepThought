package net.dankito.deepthought.javafx.ui.controls

import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.ui.view.IItemsListView


interface IItemsListViewJavaFX : IItemsListView {

    fun showItemsForTag(tag: Tag, tagsFilter: List<Tag>)

    fun showItemsForSource(source: Source)

}
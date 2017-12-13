package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag


interface IEntriesListView : IEntitiesListView<Item> {

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>)

}
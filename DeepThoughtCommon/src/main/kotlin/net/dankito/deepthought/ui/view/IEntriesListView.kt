package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag


interface IEntriesListView : IEntitiesListView<Entry> {

    fun showEntriesForTag(tag: Tag, tagsFilter: List<Tag>)

}
package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Tag


interface ITagsOnItemListView : ITagsListView {

    fun shouldCreateNotExistingTags(notExistingTags: List<String>, tagsShouldGetCreatedCallback: (tagsOnItem: MutableCollection<Tag>) -> Unit)

}
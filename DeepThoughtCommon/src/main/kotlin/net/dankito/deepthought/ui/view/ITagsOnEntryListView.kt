package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Tag


interface ITagsOnEntryListView : ITagsListView {

    fun shouldCreateNotExistingTags(notExistingTags: List<String>, tagsShouldGetCreatedCallback: (tagsOnEntry: MutableCollection<Tag>) -> Unit)

}
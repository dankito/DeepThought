package net.dankito.deepthought.ui.view

import net.dankito.deepthought.model.Tag


interface ITagsListView : IEntitiesListView<Tag> {

    fun updateDisplayedTags()

}
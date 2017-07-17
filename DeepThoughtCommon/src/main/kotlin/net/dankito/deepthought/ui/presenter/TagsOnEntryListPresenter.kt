package net.dankito.deepthought.ui.presenter

import net.dankito.deepthought.ui.tags.TagsSearchResultsUtil
import net.dankito.deepthought.ui.view.ITagsListView
import net.dankito.service.search.ISearchEngine


class TagsOnEntryListPresenter(tagsListView: ITagsListView, searchEngine: ISearchEngine, searchResultsUtil: TagsSearchResultsUtil)
    : TagsListPresenterBase(tagsListView, searchEngine, searchResultsUtil) {
}
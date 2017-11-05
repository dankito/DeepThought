package net.dankito.service.search.specific


import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag


class FilteredTagsSearchResult(val entriesHavingFilteredTags: List<Item>, val tagsOnEntriesContainingFilteredTags: List<Tag>)

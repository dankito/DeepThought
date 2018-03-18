package net.dankito.service.search.specific


import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag


class FilteredTagsSearchResult(val itemsHavingFilteredTags: List<Item>, val tagsOnItemsContainingFilteredTags: List<Tag>)

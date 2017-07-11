package net.dankito.service.search.specific


import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag


class FilteredTagsSearchResult(val entriesHavingFilteredTags: List<Entry>, val tagsOnEntriesContainingFilteredTags: List<Tag>)

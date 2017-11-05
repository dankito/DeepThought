package net.dankito.service.search

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag


data class SearchFilteredTagsTestData(val countTagsToFilter: Int, val countTagsOnEntriesWithTagsToFilter: Int, val countEntriesOnTagsToFilter: Int,
                                 val countNoiseTags: Int = 20, val countNoiseEntries: Int = 30) {

    lateinit var tagsToFilter: List<Tag>

    lateinit var tagsOnEntriesWithTagsToFilter: List<Tag>

    lateinit var entriesOnTagsToFilter: List<Item>

    lateinit var noiseTags: List<Tag>

    lateinit var noiseItems: List<Item>

}
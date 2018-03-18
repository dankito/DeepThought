package net.dankito.service.search

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag


data class SearchFilteredTagsTestData(val countTagsToFilter: Int, val countTagsOnItemsWithTagsToFilter: Int, val countItemsOnTagsToFilter: Int,
                                      val countNoiseTags: Int = 20, val countNoiseItems: Int = 30) {

    lateinit var tagsToFilter: List<Tag>

    lateinit var tagsOnItemsWithTagsToFilter: List<Tag>

    lateinit var itemsOnTagsToFilter: List<Item>

    lateinit var noiseTags: List<Tag>

    lateinit var noiseItems: List<Item>

}
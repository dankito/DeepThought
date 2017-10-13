package net.dankito.deepthought.model.util

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Source
import net.dankito.deepthought.model.Tag


data class ItemExtractionResult(var item: Item, var source: Source? = null, var series: Series? = null, var tags: MutableList<Tag> = ArrayList(),
                                var couldExtractContent: Boolean = false,
                                var error: Exception? = null) {


    private constructor() : this(Item("")) // for Jackson


    fun setExtractedContent(item: Item, source: Source?) {
        val previousItem = this.item
        val previousSource = this.source

        this.item = item
        source?.let { this.source = it }

        this.couldExtractContent = item.content.isNullOrBlank() == false // set couldExtractContent here by extracted content before previous content may gets copied over in copyInfoFromPreviousItemAndSource()

        copyInfoFromPreviousItemAndSource(item, previousItem, source, previousSource)
    }

    private fun copyInfoFromPreviousItemAndSource(item: Item, previousItem: Item, source: Source?, previousSource: Source?) {
        if(item.summary.isNullOrBlank() && previousItem != null) {
            item.summary = previousItem.summary
        }
        if(item.content.isNullOrBlank() && previousItem != null) {
            item.content = previousItem.content
        }

        if(source != null && previousSource != null) {
            if(source.title.isNullOrBlank() && previousSource.title.isNullOrBlank() == false) {
                source.title = previousSource.title
            }
            if(source.previewImageUrl == null && previousSource.previewImageUrl != null) {
                source.previewImageUrl = previousSource.previewImageUrl
            }
            if(source.publishingDate == null && previousSource.publishingDate != null) {
                source.publishingDate = previousSource.publishingDate
            }
            if(source.publishingDateString.isNullOrBlank() && previousSource.publishingDateString.isNullOrBlank() == false) {
                source.setPublishingDate(source.publishingDate, previousSource.publishingDateString)
            }
        }
    }

}
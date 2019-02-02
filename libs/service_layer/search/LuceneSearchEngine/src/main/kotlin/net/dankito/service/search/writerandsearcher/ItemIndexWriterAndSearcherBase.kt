package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.preview
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.service.data.ItemService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.dankito.utils.extensions.ofMaxLength
import org.apache.lucene.document.*
import java.text.SimpleDateFormat


abstract class ItemIndexWriterAndSearcherBase(itemService: ItemService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<Item>(itemService, eventBus, osHelper, threadPool) {

    companion object {
        @JvmStatic
        protected val MaxItemsSearchResults = 1000000 // e.g. for AllItemsCalculatedTag all items must be returned

        @JvmStatic
        protected val PublishingDateFormat = SimpleDateFormat("yyyy.MM.dd")

        @JvmStatic
        protected val MaxItemPreviewSortLength = 50
        @JvmStatic
        protected val MaxItemSummaryPreviewForSortingLength = 20
    }


    override fun addEntityFieldsToDocument(entity: Item, doc: Document) {
        val contentPlainText = entity.contentPlainText
        val summaryPlainText = entity.summaryPlainText

        doc.add(LongField(FieldName.ItemCreated, entity.createdOn.time, Field.Store.YES))

        addPreviewsForSortingToDocument(entity, contentPlainText, summaryPlainText, doc)

        addAdditionalFieldsToDocument(entity, contentPlainText, summaryPlainText, doc)
    }

    protected open fun addPreviewsForSortingToDocument(item: Item, contentPlainText: String, summaryPlainText: String, doc: Document) {
        doc.add(StringField(FieldName.ItemPreviewForSorting, contentPlainText.trim().ofMaxLength(MaxItemPreviewSortLength).toLowerCase(), Field.Store.YES))

        item.source?.series?.let {  series ->
            doc.add(Field(FieldName.ItemSeries, series.title, TextField.TYPE_NOT_STORED))
        }

        item.source?.let { source ->
            source.publishingDate?.let { publishingDate ->
                doc.add(LongField(FieldName.ItemSourcePublishingDate, publishingDate.time, Field.Store.YES))
                doc.add(Field(FieldName.ItemSourcePublishingDateString, PublishingDateFormat.format(publishingDate).toLowerCase(), TextField.TYPE_NOT_STORED))
            } ?: source.publishingDateString?.let { publishingDateString ->
                doc.add(Field(FieldName.ItemSourcePublishingDateString, publishingDateString, TextField.TYPE_NOT_STORED))
            }

            doc.add(Field(FieldName.ItemSource, source.preview, TextField.TYPE_NOT_STORED))
        }

        doc.add(StringField(FieldName.ItemIndication, item.indication.toLowerCase(), Field.Store.YES))

        doc.add(StringField(FieldName.ItemSummaryForSorting, summaryPlainText.ofMaxLength(MaxItemSummaryPreviewForSortingLength).toLowerCase(), Field.Store.YES))
    }

    protected open fun addAdditionalFieldsToDocument(item: Item, contentPlainText: String, summaryPlainText: String, doc: Document) {

    }

}
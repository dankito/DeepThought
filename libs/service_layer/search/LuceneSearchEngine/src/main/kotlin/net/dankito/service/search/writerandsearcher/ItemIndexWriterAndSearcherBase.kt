package net.dankito.service.search.writerandsearcher

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.extensions.contentPlainText
import net.dankito.deepthought.model.extensions.sourcePreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.extensions.summaryPlainText
import net.dankito.service.data.ItemService
import net.dankito.service.eventbus.IEventBus
import net.dankito.service.search.FieldName
import net.dankito.utils.IThreadPool
import net.dankito.utils.OsHelper
import net.dankito.utils.extensions.ofMaxLength
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.StringField


abstract class ItemIndexWriterAndSearcherBase(itemService: ItemService, eventBus: IEventBus, osHelper: OsHelper, threadPool: IThreadPool)
    : IndexWriterAndSearcher<Item>(itemService, eventBus, osHelper, threadPool) {

    companion object {
        @JvmStatic
        protected val MaxItemsSearchResults = 1000000 // e.g. for AllItemsCalculatedTag all items must be returned

        private val MaxItemPreviewSortLength = 75
        private val MaxSourcePreviewSortLength = 75
    }


    override fun addEntityFieldsToDocument(entity: Item, doc: Document) {
        val contentPlainText = entity.contentPlainText
        val summaryPlainText = entity.summaryPlainText

        doc.add(LongField(FieldName.ItemCreated, entity.createdOn.time, Field.Store.YES))

        addPreviewsForSortingToDocument(entity, contentPlainText, summaryPlainText, doc)

        addAdditionalFieldsToDocument(entity, contentPlainText, summaryPlainText, doc)
    }

    protected open fun addPreviewsForSortingToDocument(item: Item, contentPlainText: String, summaryPlainText: String, doc: Document) {
        doc.add(StringField(FieldName.ItemPreviewForSorting, contentPlainText.ofMaxLength(MaxItemPreviewSortLength), Field.Store.YES))

        val sourceOrAbstractPreview = (item.sourcePreviewWithSeriesAndPublishingDate + " " + summaryPlainText).ofMaxLength(MaxSourcePreviewSortLength)
        doc.add(StringField(FieldName.ItemSourcePreviewForSorting, sourceOrAbstractPreview, Field.Store.YES))
    }

    protected open fun addAdditionalFieldsToDocument(item: Item, contentPlainText: String, summaryPlainText: String, doc: Document) {

    }

}
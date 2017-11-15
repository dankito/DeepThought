package net.dankito.service.data

import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.extensions.getEntryPreview
import net.dankito.deepthought.model.extensions.getEntryPreviewWithSeriesAndPublishingDate
import net.dankito.deepthought.model.util.ItemExtractionResult
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.utils.serialization.ISerializer
import org.slf4j.LoggerFactory


class ReadLaterArticleService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier, private val serializer: ISerializer)
    : EntityServiceBase<ReadLaterArticle>(ReadLaterArticle::class.java, dataManager, entityChangedNotifier) {

    companion object {
        private val log = LoggerFactory.getLogger(ReadLaterArticleService::class.java)
    }


    override fun onPrePersist(entity: ReadLaterArticle) {
        super.onPrePersist(entity)

        val extractionResult = entity.itemExtractionResult
        extractionResult.item.preview = extractionResult.item.getEntryPreview(true)
        entity.itemPreview = extractionResult.item.getEntryPreviewWithSeriesAndPublishingDate(extractionResult.source, extractionResult.series)

        entity.serializedItemExtractionResult = serializer.serializeObject(entity.itemExtractionResult)
    }

    override fun getAll(): List<ReadLaterArticle> {
        val readLaterArticles = super.getAll()

        readLaterArticles.forEach { article -> deserializeEntryExtractionResult(article) }

        return readLaterArticles
    }

    override fun retrieve(id: String): ReadLaterArticle? {
        val article = super.retrieve(id)

        article?.let { deserializeEntryExtractionResult(it) }

        return article
    }


    fun deserializeEntryExtractionResult(readLaterArticle: ReadLaterArticle) {
        if(readLaterArticle.itemExtractionResult.item.content.isBlank()) { // itemExtractionResult not deserialized yet
            try {
                readLaterArticle.itemExtractionResult = serializer.deserializeObject(readLaterArticle.serializedItemExtractionResult, ItemExtractionResult::class.java)
            } catch(e: Exception) {
                log.error("Could not deserialize ReadLaterArticle $readLaterArticle", e)
            }
        }
    }

}
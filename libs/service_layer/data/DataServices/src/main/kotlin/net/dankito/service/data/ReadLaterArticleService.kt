package net.dankito.service.data

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.deepthought.model.ReadLaterArticle
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.model.util.EntryExtractionResult
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.data.event.EntityChangedNotifier
import net.dankito.service.data.serialization.PersistedTagDeserializer
import net.dankito.service.data.serialization.PersistedTagSerializer


class ReadLaterArticleService(dataManager: DataManager, entityChangedNotifier: EntityChangedNotifier, tagService: TagService)
    : EntityServiceBase<ReadLaterArticle>(ReadLaterArticle::class.java, dataManager, entityChangedNotifier) {

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val module = SimpleModule()

        module.addSerializer(Tag::class.java, PersistedTagSerializer())
        module.addDeserializer(Tag::class.java, PersistedTagDeserializer(tagService))

        objectMapper.registerModule(module)
    }


    override fun onPrePersist(entity: ReadLaterArticle) {
        super.onPrePersist(entity)

        entity.serializedEntryExtractionResult = objectMapper.writeValueAsString(entity.entryExtractionResult)
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
        if(readLaterArticle.entryExtractionResult.entry.content.isBlank()) { // entryExtractionResult not deserialized yet
            readLaterArticle.entryExtractionResult = objectMapper.readValue(readLaterArticle.serializedEntryExtractionResult, EntryExtractionResult::class.java)
        }
    }

}
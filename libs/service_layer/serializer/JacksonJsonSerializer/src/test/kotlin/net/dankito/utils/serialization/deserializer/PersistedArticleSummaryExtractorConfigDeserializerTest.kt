package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.service.data.ArticleSummaryExtractorConfigService
import net.dankito.service.data.EntityServiceBase
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedArticleSummaryExtractorConfigDeserializerTest : PersistedEntityDeserializerTestBase<ArticleSummaryExtractorConfig>(ArticleSummaryExtractorConfig::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<ArticleSummaryExtractorConfig> {
        return mock(ArticleSummaryExtractorConfigService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<ArticleSummaryExtractorConfig>): PersistedEntityDeserializerBase<ArticleSummaryExtractorConfig> {
        return PersistedArticleSummaryExtractorConfigDeserializer(entityService as ArticleSummaryExtractorConfigService, mock(StdDeserializer::class.java) as StdDeserializer<ArticleSummaryExtractorConfig>)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.ArticleSummaryExtractorConfigIdFieldName
    }

}
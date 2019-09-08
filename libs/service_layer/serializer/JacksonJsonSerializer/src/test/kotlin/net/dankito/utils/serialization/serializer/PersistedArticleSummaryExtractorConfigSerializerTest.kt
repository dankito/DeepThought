package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedArticleSummaryExtractorConfigSerializerTest : PersistedEntitySerializerTestBase<ArticleSummaryExtractorConfig>(ArticleSummaryExtractorConfig::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<ArticleSummaryExtractorConfig> {
        return PersistedArticleSummaryExtractorConfigSerializer(mock(StdSerializer::class.java) as StdSerializer<ArticleSummaryExtractorConfig>)
    }

    override fun createEntity(): ArticleSummaryExtractorConfig {
        return ArticleSummaryExtractorConfig("https://www.example.com", "name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.ArticleSummaryExtractorConfigIdFieldName
    }

}
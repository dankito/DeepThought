package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.service.data.ArticleSummaryExtractorConfigService
import net.dankito.utils.serialization.SerializerConfig


class PersistedArticleSummaryExtractorConfigDeserializer(service: ArticleSummaryExtractorConfigService, defaultDeserializer: StdDeserializer<ArticleSummaryExtractorConfig>)
    : PersistedEntityDeserializerBase<ArticleSummaryExtractorConfig>(service, defaultDeserializer, SerializerConfig.ArticleSummaryExtractorConfigIdFieldName, ArticleSummaryExtractorConfig::class.java)
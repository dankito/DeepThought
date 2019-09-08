package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.utils.serialization.SerializerConfig


class PersistedArticleSummaryExtractorConfigSerializer(defaultSerializer: StdSerializer<ArticleSummaryExtractorConfig>)
    : PersistedEntitySerializerBase<ArticleSummaryExtractorConfig>(defaultSerializer, SerializerConfig.ArticleSummaryExtractorConfigIdFieldName, ArticleSummaryExtractorConfig::class.java)
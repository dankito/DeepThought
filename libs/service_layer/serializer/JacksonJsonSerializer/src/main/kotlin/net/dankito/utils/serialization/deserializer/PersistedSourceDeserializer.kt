package net.dankito.utils.serialization.deserializer

import net.dankito.deepthought.model.Source
import net.dankito.service.data.SourceService
import net.dankito.utils.serialization.SerializerConfig


class PersistedSourceDeserializer(sourceService: SourceService)
    : PersistedEntityDeserializerBase<Source>(sourceService, SerializerConfig.SourceIdFieldName, Source::class.java)
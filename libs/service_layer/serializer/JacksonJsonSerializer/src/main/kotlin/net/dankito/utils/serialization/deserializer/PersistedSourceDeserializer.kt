package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Source
import net.dankito.service.data.SourceService
import net.dankito.utils.serialization.SerializerConfig


class PersistedSourceDeserializer(sourceService: SourceService, defaultDeserializer: StdDeserializer<Source>)
    : PersistedEntityDeserializerBase<Source>(sourceService, defaultDeserializer, SerializerConfig.SourceIdFieldName, Source::class.java)
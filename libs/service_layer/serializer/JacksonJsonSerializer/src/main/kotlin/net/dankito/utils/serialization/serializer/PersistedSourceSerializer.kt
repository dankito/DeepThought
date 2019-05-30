package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Source
import net.dankito.utils.serialization.SerializerConfig


class PersistedSourceSerializer(defaultSerializer: StdSerializer<Source>)
    : PersistedEntitySerializerBase<Source>(defaultSerializer, SerializerConfig.SourceIdFieldName, Source::class.java)
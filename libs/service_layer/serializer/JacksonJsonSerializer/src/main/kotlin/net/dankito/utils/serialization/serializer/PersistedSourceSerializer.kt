package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Source
import net.dankito.utils.serialization.SerializerConfig


class PersistedSourceSerializer : PersistedEntitySerializerBase<Source>(SerializerConfig.SourceIdFieldName, Source::class.java)
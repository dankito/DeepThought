package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Tag
import net.dankito.utils.serialization.SerializerConfig


class PersistedTagSerializer(defaultSerializer: StdSerializer<Tag>)
    : PersistedEntitySerializerBase<Tag>(defaultSerializer, SerializerConfig.TagIdFieldName, Tag::class.java)
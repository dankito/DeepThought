package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService
import net.dankito.utils.serialization.SerializerConfig


class PersistedTagDeserializer(tagService: TagService, defaultDeserializer: StdDeserializer<Tag>)
    : PersistedEntityDeserializerBase<Tag>(tagService, defaultDeserializer, SerializerConfig.TagIdFieldName, Tag::class.java)
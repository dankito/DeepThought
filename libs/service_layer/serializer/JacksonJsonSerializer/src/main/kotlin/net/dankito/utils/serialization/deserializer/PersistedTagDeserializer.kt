package net.dankito.utils.serialization.deserializer

import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService
import net.dankito.utils.serialization.SerializerConfig


class PersistedTagDeserializer(tagService: TagService)
    : PersistedEntityDeserializerBase<Tag>(tagService, SerializerConfig.TagIdFieldName, Tag::class.java)
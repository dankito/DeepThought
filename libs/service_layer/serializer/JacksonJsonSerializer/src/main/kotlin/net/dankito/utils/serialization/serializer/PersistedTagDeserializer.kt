package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Tag
import net.dankito.service.data.TagService


class PersistedTagDeserializer(tagService: TagService)
    : PersistedEntityDeserializerBase<Tag>(tagService, SerializerConfig.TagIdFieldName, Tag::class.java)
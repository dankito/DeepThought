package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Tag
import net.dankito.utils.serialization.SerializerConfig


class PersistedTagSerializer : PersistedEntitySerializerBase<Tag>(SerializerConfig.TagIdFieldName, Tag::class.java)
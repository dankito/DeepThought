package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Tag


class PersistedTagSerializer : PersistedEntitySerializerBase<Tag>(SerializerConfig.TagIdFieldName, Tag::class.java)
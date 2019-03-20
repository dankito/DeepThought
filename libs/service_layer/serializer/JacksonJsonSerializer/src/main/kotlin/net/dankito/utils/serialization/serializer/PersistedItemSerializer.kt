package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Item
import net.dankito.utils.serialization.SerializerConfig


class PersistedItemSerializer : PersistedEntitySerializerBase<Item>(SerializerConfig.ItemIdFieldName, Item::class.java)
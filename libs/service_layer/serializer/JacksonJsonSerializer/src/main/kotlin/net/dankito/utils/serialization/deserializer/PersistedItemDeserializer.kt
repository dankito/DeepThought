package net.dankito.utils.serialization.deserializer

import net.dankito.deepthought.model.Item
import net.dankito.service.data.ItemService
import net.dankito.utils.serialization.SerializerConfig


class PersistedItemDeserializer(itemService: ItemService)
    : PersistedEntityDeserializerBase<Item>(itemService, SerializerConfig.ItemIdFieldName, Item::class.java)
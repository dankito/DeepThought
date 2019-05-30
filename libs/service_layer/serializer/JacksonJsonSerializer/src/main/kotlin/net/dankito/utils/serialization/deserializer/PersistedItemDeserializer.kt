package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Item
import net.dankito.service.data.ItemService
import net.dankito.utils.serialization.SerializerConfig


class PersistedItemDeserializer(itemService: ItemService, defaultDeserializer: StdDeserializer<Item>)
    : PersistedEntityDeserializerBase<Item>(itemService, defaultDeserializer, SerializerConfig.ItemIdFieldName, Item::class.java)
package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Item
import net.dankito.utils.serialization.SerializerConfig


class PersistedItemSerializer(defaultSerializer: StdSerializer<Item>)
    : PersistedEntitySerializerBase<Item>(defaultSerializer, SerializerConfig.ItemIdFieldName, Item::class.java)
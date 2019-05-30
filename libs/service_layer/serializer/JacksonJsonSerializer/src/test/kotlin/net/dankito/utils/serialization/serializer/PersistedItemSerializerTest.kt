package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Item
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedItemSerializerTest : PersistedEntitySerializerTestBase<Item>(Item::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Item> {
        return PersistedItemSerializer(mock(StdSerializer::class.java) as StdSerializer<Item>)
    }

    override fun createEntity(): Item {
        return Item("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.ItemIdFieldName
    }

}
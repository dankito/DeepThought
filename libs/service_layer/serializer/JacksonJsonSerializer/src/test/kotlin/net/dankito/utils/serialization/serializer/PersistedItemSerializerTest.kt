package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Item
import net.dankito.deepthought.model.Tag
import net.dankito.utils.serialization.SerializerConfig


class PersistedItemSerializerTest : PersistedEntitySerializerTestBase<Item>(Item::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Item> {
        return PersistedItemSerializer()
    }

    override fun createEntity(): Item {
        return Item("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.ItemIdFieldName
    }

}
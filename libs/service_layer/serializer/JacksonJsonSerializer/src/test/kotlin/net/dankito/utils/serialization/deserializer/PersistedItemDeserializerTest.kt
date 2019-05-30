package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Item
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.ItemService
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedItemDeserializerTest : PersistedEntityDeserializerTestBase<Item>(Item::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<Item> {
        return mock(ItemService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<Item>): PersistedEntityDeserializerBase<Item> {
        return PersistedItemDeserializer(entityService as ItemService, mock(StdDeserializer::class.java) as StdDeserializer<Item>)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.ItemIdFieldName
    }

}
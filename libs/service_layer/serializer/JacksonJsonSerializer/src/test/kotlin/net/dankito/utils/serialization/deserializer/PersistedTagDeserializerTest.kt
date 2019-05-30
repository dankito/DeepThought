package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.TagService
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedTagDeserializerTest : PersistedEntityDeserializerTestBase<Tag>(Tag::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<Tag> {
        return mock(TagService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<Tag>): PersistedEntityDeserializerBase<Tag> {
        return PersistedTagDeserializer(entityService as TagService, mock(StdDeserializer::class.java) as StdDeserializer<Tag>)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.TagIdFieldName
    }

}
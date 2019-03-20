package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Tag
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.TagService
import org.mockito.Mockito.mock


class PersistedTagDeserializerTest : PersistedEntityDeserializerTestBase<Tag>(Tag::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<Tag> {
        return mock(TagService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<Tag>): PersistedEntityDeserializerBase<Tag> {
        return PersistedTagDeserializer(entityService as TagService)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.TagIdFieldName
    }

}
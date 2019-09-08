package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.Source
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.SourceService
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedSourceDeserializerTest : PersistedEntityDeserializerTestBase<Source>(Source::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<Source> {
        return mock(SourceService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<Source>): PersistedEntityDeserializerBase<Source> {
        return PersistedSourceDeserializer(entityService as SourceService, mock(StdDeserializer::class.java) as StdDeserializer<Source>)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SourceIdFieldName
    }

}
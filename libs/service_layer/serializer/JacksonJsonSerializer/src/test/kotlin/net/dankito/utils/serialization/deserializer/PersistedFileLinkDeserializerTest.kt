package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.FileLink
import net.dankito.service.data.EntityServiceBase
import net.dankito.service.data.FileService
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedFileLinkDeserializerTest : PersistedEntityDeserializerTestBase<FileLink>(FileLink::class.java) {

    override fun createEntityServiceMock(): EntityServiceBase<FileLink> {
        return mock(FileService::class.java)
    }

    override fun createDeserializer(entityService: EntityServiceBase<FileLink>): PersistedEntityDeserializerBase<FileLink> {
        return PersistedFileLinkDeserializer(entityService as FileService, mock(StdDeserializer::class.java) as StdDeserializer<FileLink>)
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.FileLinkIdFieldName
    }

}
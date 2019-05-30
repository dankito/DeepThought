package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.FileLink
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedFileLinkSerializerTest : PersistedEntitySerializerTestBase<FileLink>(FileLink::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<FileLink> {
        return PersistedFileLinkSerializer(mock(StdSerializer::class.java) as StdSerializer<FileLink>)
    }

    override fun createEntity(): FileLink {
        return FileLink("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.FileLinkIdFieldName
    }

}
package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Source
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedSourceSerializerTest : PersistedEntitySerializerTestBase<Source>(Source::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Source> {
        return PersistedSourceSerializer(mock(StdSerializer::class.java) as StdSerializer<Source>)
    }

    override fun createEntity(): Source {
        return Source("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SourceIdFieldName
    }

}
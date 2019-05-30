package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Tag
import net.dankito.utils.serialization.SerializerConfig
import org.mockito.Mockito.mock


class PersistedTagSerializerTest : PersistedEntitySerializerTestBase<Tag>(Tag::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Tag> {
        return PersistedTagSerializer(mock(StdSerializer::class.java) as StdSerializer<Tag>)
    }

    override fun createEntity(): Tag {
        return Tag("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.TagIdFieldName
    }

}
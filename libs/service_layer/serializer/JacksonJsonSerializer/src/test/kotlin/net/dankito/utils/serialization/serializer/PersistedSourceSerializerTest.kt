package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Source
import net.dankito.utils.serialization.SerializerConfig


class PersistedSourceSerializerTest : PersistedEntitySerializerTestBase<Source>(Source::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Source> {
        return PersistedSourceSerializer()
    }

    override fun createEntity(): Source {
        return Source("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.SourceIdFieldName
    }

}
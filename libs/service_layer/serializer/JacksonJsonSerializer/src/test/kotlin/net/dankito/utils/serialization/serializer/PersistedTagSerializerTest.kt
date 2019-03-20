package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.Tag


class PersistedTagSerializerTest : PersistedEntitySerializerTestBase<Tag>(Tag::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<Tag> {
        return PersistedTagSerializer()
    }

    override fun createEntity(): Tag {
        return Tag("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.TagIdFieldName
    }

}
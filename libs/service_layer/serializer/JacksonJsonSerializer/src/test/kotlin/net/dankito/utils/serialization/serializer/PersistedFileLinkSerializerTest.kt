package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.FileLink
import net.dankito.utils.serialization.SerializerConfig


class PersistedFileLinkSerializerTest : PersistedEntitySerializerTestBase<FileLink>(FileLink::class.java) {

    override fun createSerializer(): PersistedEntitySerializerBase<FileLink> {
        return PersistedFileLinkSerializer()
    }

    override fun createEntity(): FileLink {
        return FileLink("Name")
    }

    override fun getIdFieldName(): String {
        return SerializerConfig.FileLinkIdFieldName
    }

}
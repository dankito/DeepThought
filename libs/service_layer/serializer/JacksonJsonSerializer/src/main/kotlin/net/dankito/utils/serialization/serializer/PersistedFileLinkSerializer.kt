package net.dankito.utils.serialization.serializer

import net.dankito.deepthought.model.FileLink
import net.dankito.utils.serialization.SerializerConfig


class PersistedFileLinkSerializer : PersistedEntitySerializerBase<FileLink>(SerializerConfig.FileLinkIdFieldName, FileLink::class.java)
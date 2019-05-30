package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.FileLink
import net.dankito.utils.serialization.SerializerConfig


class PersistedFileLinkSerializer(defaultSerializer: StdSerializer<FileLink>)
    : PersistedEntitySerializerBase<FileLink>(defaultSerializer, SerializerConfig.FileLinkIdFieldName, FileLink::class.java)
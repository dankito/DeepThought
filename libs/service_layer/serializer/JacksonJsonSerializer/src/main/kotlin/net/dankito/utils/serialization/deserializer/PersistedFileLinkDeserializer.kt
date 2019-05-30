package net.dankito.utils.serialization.deserializer

import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import net.dankito.deepthought.model.FileLink
import net.dankito.service.data.FileService
import net.dankito.utils.serialization.SerializerConfig


class PersistedFileLinkDeserializer(fileService: FileService, defaultDeserializer: StdDeserializer<FileLink>)
    : PersistedEntityDeserializerBase<FileLink>(fileService, defaultDeserializer, SerializerConfig.FileLinkIdFieldName, FileLink::class.java)
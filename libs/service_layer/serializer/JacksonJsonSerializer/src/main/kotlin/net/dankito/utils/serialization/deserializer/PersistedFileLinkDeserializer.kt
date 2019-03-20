package net.dankito.utils.serialization.deserializer

import net.dankito.deepthought.model.FileLink
import net.dankito.service.data.FileService
import net.dankito.utils.serialization.SerializerConfig


class PersistedFileLinkDeserializer(fileService: FileService)
    : PersistedEntityDeserializerBase<FileLink>(fileService, SerializerConfig.FileLinkIdFieldName, FileLink::class.java)
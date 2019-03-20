package net.dankito.deepthought.utils

import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.deepthought.model.*
import net.dankito.service.data.*
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.serialization.deserializer.*
import net.dankito.utils.serialization.serializer.*


class DeepThoughtJacksonJsonSerializer(itemService: ItemService, tagService: TagService, sourceService: SourceService,
                                       seriesService: SeriesService, fileService: FileService)
    : JacksonJsonSerializer(configureMapperCallback = { mapper ->

    val module = SimpleModule() // TODO: find a better place for this

    module.addSerializer(Item::class.java, PersistedItemSerializer())
    module.addDeserializer(Item::class.java, PersistedItemDeserializer(itemService))

    module.addSerializer(Tag::class.java, PersistedTagSerializer())
    module.addDeserializer(Tag::class.java, PersistedTagDeserializer(tagService))

    module.addSerializer(Source::class.java, PersistedSourceSerializer())
    module.addDeserializer(Source::class.java, PersistedSourceDeserializer(sourceService))

    module.addSerializer(Series::class.java, PersistedSeriesSerializer())
    module.addDeserializer(Series::class.java, PersistedSeriesDeserializer(seriesService))

    module.addSerializer(FileLink::class.java, PersistedFileLinkSerializer())
    module.addDeserializer(FileLink::class.java, PersistedFileLinkDeserializer(fileService))

    mapper.registerModule(module)
})
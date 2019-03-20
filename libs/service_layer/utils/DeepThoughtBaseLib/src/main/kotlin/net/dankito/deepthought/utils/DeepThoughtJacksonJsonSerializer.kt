package net.dankito.deepthought.utils

import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.SeriesService
import net.dankito.service.data.TagService
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.serialization.deserializer.PersistedSeriesDeserializer
import net.dankito.utils.serialization.deserializer.PersistedTagDeserializer
import net.dankito.utils.serialization.serializer.PersistedSeriesSerializer
import net.dankito.utils.serialization.serializer.PersistedTagSerializer


class DeepThoughtJacksonJsonSerializer(tagService: TagService, seriesService: SeriesService)
    : JacksonJsonSerializer(configureMapperCallback = { mapper ->

    val module = SimpleModule() // TODO: find a better place for this

    module.addSerializer(Tag::class.java, PersistedTagSerializer())
    module.addDeserializer(Tag::class.java, PersistedTagDeserializer(tagService))
    module.addSerializer(Series::class.java, PersistedSeriesSerializer())
    module.addDeserializer(Series::class.java, PersistedSeriesDeserializer(seriesService))

    mapper.registerModule(module)
})
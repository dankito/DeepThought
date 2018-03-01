package net.dankito.deepthought.serialization

import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.deepthought.serialization.serializer.PersistedSeriesDeserializer
import net.dankito.deepthought.serialization.serializer.PersistedSeriesSerializer
import net.dankito.deepthought.serialization.serializer.PersistedTagDeserializer
import net.dankito.deepthought.serialization.serializer.PersistedTagSerializer
import net.dankito.service.data.SeriesService
import net.dankito.service.data.TagService
import net.dankito.util.serialization.ISerializer
import net.dankito.util.serialization.JacksonJsonSerializer


class DeepThoughtJacksonJsonSerializer(tagService: TagService, seriesService: SeriesService) : JacksonJsonSerializer(), ISerializer {

    init {
        val module = SimpleModule()

        module.addSerializer(Tag::class.java, PersistedTagSerializer())
        module.addDeserializer(Tag::class.java, PersistedTagDeserializer(tagService))
        module.addSerializer(Series::class.java, PersistedSeriesSerializer())
        module.addDeserializer(Series::class.java, PersistedSeriesDeserializer(seriesService))

        objectMapper.registerModule(module)
    }

}
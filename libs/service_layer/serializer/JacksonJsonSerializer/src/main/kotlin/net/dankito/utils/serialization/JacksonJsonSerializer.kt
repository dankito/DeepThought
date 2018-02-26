package net.dankito.utils.serialization

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import net.dankito.deepthought.model.Series
import net.dankito.deepthought.model.Tag
import net.dankito.service.data.SeriesService
import net.dankito.service.data.TagService
import net.dankito.util.serialization.ISerializer
import net.dankito.utils.serialization.serializer.PersistedSeriesDeserializer
import net.dankito.utils.serialization.serializer.PersistedSeriesSerializer
import net.dankito.utils.serialization.serializer.PersistedTagDeserializer
import net.dankito.utils.serialization.serializer.PersistedTagSerializer


class JacksonJsonSerializer(tagService: TagService, seriesService: SeriesService) : ISerializer {

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // only serialize fields
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        val module = SimpleModule()

        module.addSerializer(Tag::class.java, PersistedTagSerializer())
        module.addDeserializer(Tag::class.java, PersistedTagDeserializer(tagService))
        module.addSerializer(Series::class.java, PersistedSeriesSerializer())
        module.addDeserializer(Series::class.java, PersistedSeriesDeserializer(seriesService))

        objectMapper.registerModule(module)
    }


    override fun serializeObject(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    override fun <T> deserializeObject(serializedObject: String, objectClass: Class<T>, vararg genericParameterTypes: Class<*>): T {
        if(genericParameterTypes.isEmpty()) {
            return objectMapper.readValue(serializedObject, objectClass)
        }
        else {
            return objectMapper.readValue<T>(serializedObject, objectMapper.typeFactory.constructParametricType(objectClass, *genericParameterTypes))
        }
    }

}
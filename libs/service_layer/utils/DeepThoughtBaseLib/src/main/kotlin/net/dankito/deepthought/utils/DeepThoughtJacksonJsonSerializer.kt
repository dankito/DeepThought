package net.dankito.deepthought.utils

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializer
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.*
import net.dankito.service.data.*
import net.dankito.utils.serialization.JacksonJsonSerializer
import net.dankito.utils.serialization.deserializer.*
import net.dankito.utils.serialization.serializer.*


class DeepThoughtJacksonJsonSerializer(itemService: ItemService, tagService: TagService, sourceService: SourceService,
                                       seriesService: SeriesService, fileService: FileService)
    : JacksonJsonSerializer(configureMapperCallback = { mapper ->


    val module = object : SimpleModule() { // TODO: find a better place for this

        override fun setupModule(context: SetupContext) {
            super.setupModule(context)

            context.addBeanSerializerModifier(object : BeanSerializerModifier() {

                override fun modifySerializer(config: SerializationConfig, beanDesc: BeanDescription, serializer: JsonSerializer<*>): JsonSerializer<*> {
                    if (serializer is BeanSerializer) {
                        when (serializer.handledType()) {
                            Item::class.java -> return PersistedItemSerializer(serializer as StdSerializer<Item>)
                            Tag::class.java -> return PersistedTagSerializer(serializer as StdSerializer<Tag>)
                            Source::class.java -> return PersistedSourceSerializer(serializer as StdSerializer<Source>)
                            Series::class.java -> return PersistedSeriesSerializer(serializer as StdSerializer<Series>)
                            FileLink::class.java -> return PersistedFileLinkSerializer(serializer as StdSerializer<FileLink>)
                        }
                    }

                    return super.modifySerializer(config, beanDesc, serializer)
                }

            })

            context.addBeanDeserializerModifier(object : BeanDeserializerModifier() {

                override fun modifyDeserializer(config: DeserializationConfig?, beanDesc: BeanDescription?, deserializer: JsonDeserializer<*>?): JsonDeserializer<*> {
                    if (deserializer is BeanDeserializer) {
                        when (deserializer.handledType()) {
                            Item::class.java -> return PersistedItemDeserializer(itemService, deserializer as StdDeserializer<Item>)
                            Tag::class.java -> return PersistedTagDeserializer(tagService, deserializer as StdDeserializer<Tag>)
                            Source::class.java -> return PersistedSourceDeserializer(sourceService, deserializer as StdDeserializer<Source>)
                            Series::class.java -> return PersistedSeriesDeserializer(seriesService, deserializer as StdDeserializer<Series>)
                            FileLink::class.java -> return PersistedFileLinkDeserializer(fileService, deserializer as StdDeserializer<FileLink>)
                        }
                    }

                    return super.modifyDeserializer(config, beanDesc, deserializer)
                }

            })
        }
    }

    mapper.registerModule(module)
})
package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Tag


class PersistedTagSerializer : StdSerializer<Tag>(Tag::class.java) {

    override fun serialize(value: Tag?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { value ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.TagIdFieldName, value.id)

                jsonGenerator.writeEndObject()
            }
        }
    }

}
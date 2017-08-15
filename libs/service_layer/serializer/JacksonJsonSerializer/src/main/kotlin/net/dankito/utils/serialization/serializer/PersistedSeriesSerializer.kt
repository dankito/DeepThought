package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.Series


class PersistedSeriesSerializer : StdSerializer<Series>(Series::class.java) {

    override fun serialize(value: Series?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { value ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(SerializerConfig.SeriesIdFieldName, value.id)

                jsonGenerator.writeEndObject()
            }
        }
    }

}
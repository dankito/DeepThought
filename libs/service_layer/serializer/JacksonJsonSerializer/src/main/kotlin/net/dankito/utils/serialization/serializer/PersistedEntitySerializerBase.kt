package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.BaseEntity


open class PersistedEntitySerializerBase<TEntity : BaseEntity>(protected val idFieldName: String, entityClass: Class<TEntity>)
    : StdSerializer<TEntity>(entityClass) {

    override fun serialize(value: TEntity?, gen: JsonGenerator?, provider: SerializerProvider?) {
        value?.let { value ->
            gen?.let { jsonGenerator ->
                jsonGenerator.writeStartObject()

                jsonGenerator.writeStringField(idFieldName, value.id)

                jsonGenerator.writeEndObject()
            }
        }
    }

}
package net.dankito.utils.serialization.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.dankito.deepthought.model.BaseEntity


open class PersistedEntitySerializerBase<TEntity : BaseEntity>(protected val defaultSerializer: StdSerializer<TEntity>,
                                                               protected val idFieldName: String,
                                                               entityClass: Class<TEntity>)
    : StdSerializer<TEntity>(entityClass) {


    override fun serialize(value: TEntity?, jsonGenerator: JsonGenerator, provider: SerializerProvider?) {
        if (value?.isPersisted() == true) {
            jsonGenerator.writeStartObject()

            jsonGenerator.writeStringField(idFieldName, value.id)

            jsonGenerator.writeEndObject()
        }
        else { // use default serialization if entity is not persisted
            defaultSerializer.serialize(value, jsonGenerator, provider)
        }
    }

}
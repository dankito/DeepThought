package net.dankito.utils.serialization

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper


class JacksonJsonSerializer : ISerializer {

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // only serialize fields
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
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
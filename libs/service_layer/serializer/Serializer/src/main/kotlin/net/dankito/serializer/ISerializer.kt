package net.dankito.serializer


interface ISerializer {

    fun serializeObject(obj: Any) : String

    fun <T> deserializeObject(serializedObject: String, objectClass: Class<T>, vararg genericParameterTypes: Class<*>) : T

}
package net.dankito.util.serialization


interface ISerializer {

    fun serializeObject(obj: Any) : String

    fun <T> deserializeObject(serializedObject: String, objectClass: Class<T>, vararg genericParameterTypes: Class<*>) : T

}
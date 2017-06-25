package net.dankito.utils.services.hashing


interface IBase64Service {

    fun encode(stringToEncode: String): String

    fun encode(dataToEncode: ByteArray): String

    fun decode(stringToDecode: String): String

    fun decodeToBytes(stringToDecode: String): ByteArray

}

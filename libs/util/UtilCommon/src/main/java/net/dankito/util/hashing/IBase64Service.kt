package net.dankito.util.hashing

import java.nio.charset.Charset


interface IBase64Service {

    companion object {
        val DEFAULT_CHAR_SET: Charset = Charset.forName("UTF-8")
    }


    fun encode(stringToEncode: String): String

    fun encode(dataToEncode: ByteArray): String

    fun decode(stringToDecode: String): String

    fun decodeToBytes(stringToDecode: String): ByteArray

}

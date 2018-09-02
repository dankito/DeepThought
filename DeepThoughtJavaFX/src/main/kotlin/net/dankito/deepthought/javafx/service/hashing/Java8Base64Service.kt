package net.dankito.deepthought.javafx.service.hashing

import net.dankito.utils.hashing.IBase64Service
import net.dankito.utils.hashing.IBase64Service.Companion.DEFAULT_CHAR_SET
import java.util.*


class Java8Base64Service : IBase64Service {

    override fun encode(stringToEncode: String): String {
        return encode(stringToEncode.toByteArray(DEFAULT_CHAR_SET))
    }

    override fun encode(dataToEncode: ByteArray): String {
        return Base64.getEncoder().encodeToString(dataToEncode)
    }

    override fun decode(stringToDecode: String): String {
        return String(decodeToBytes(stringToDecode), DEFAULT_CHAR_SET)
    }

    override fun decodeToBytes(stringToDecode: String): ByteArray {
        return Base64.getDecoder().decode(stringToDecode.toByteArray(DEFAULT_CHAR_SET))
    }

}

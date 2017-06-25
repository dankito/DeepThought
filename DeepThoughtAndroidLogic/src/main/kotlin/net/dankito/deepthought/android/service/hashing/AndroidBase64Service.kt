package net.dankito.deepthought.android.service.hashing

import android.util.Base64

import net.dankito.utils.services.hashing.IBase64Service
import net.dankito.utils.services.hashing.IBase64Service.Companion.DEFAULT_CHAR_SET

import java.nio.charset.Charset


class AndroidBase64Service : IBase64Service {

    override fun encode(stringToEncode: String): String {
        return encode(stringToEncode.toByteArray(DEFAULT_CHAR_SET))
    }

    override fun encode(dataToEncode: ByteArray): String {
        return Base64.encodeToString(dataToEncode, Base64.NO_WRAP)
    }

    override fun decode(stringToDecode: String): String {
        return String(decodeToBytes(stringToDecode), DEFAULT_CHAR_SET)
    }

    override fun decodeToBytes(stringToDecode: String): ByteArray {
        return Base64.decode(stringToDecode, Base64.NO_WRAP)
    }

}

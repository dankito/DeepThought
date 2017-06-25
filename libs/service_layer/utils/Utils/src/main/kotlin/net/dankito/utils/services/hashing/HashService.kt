package net.dankito.utils.services.hashing


import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class HashService {

    companion object {
        val DIGEST_CHAR_SET = Charset.forName("UTF-8")
    }


    @Throws(NoSuchAlgorithmException::class)
    fun hashString(hashAlgorithm: HashAlgorithm, stringToHash: String): String {
        return String(hashStringToBytes(hashAlgorithm, stringToHash), DIGEST_CHAR_SET)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun hashStringToBytes(hashAlgorithm: HashAlgorithm, stringToHash: String): ByteArray {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm.algorithmName)
        val stringToHashBytes = stringToHash.toByteArray(DIGEST_CHAR_SET)

        messageDigest.update(stringToHashBytes)

        return messageDigest.digest()
    }

}

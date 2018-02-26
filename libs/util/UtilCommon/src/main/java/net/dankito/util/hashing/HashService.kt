package net.dankito.util.hashing


import java.io.*
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

    @Throws(NoSuchAlgorithmException::class, IOException::class, FileNotFoundException::class)
    fun getFileHash(hashAlgorithm: HashAlgorithm, file: File): String {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm.algorithmName)

        val inputStream = BufferedInputStream(FileInputStream(file))
        val buffer = ByteArray(2 * 1024)

        var bytesRead = inputStream.read(buffer)
        while(bytesRead > 0) {
            messageDigest.update(buffer, 0, bytesRead)

            bytesRead = inputStream.read(buffer)
        }

        inputStream.close()

        val digestBytes = messageDigest.digest()
        val hexString = StringBuilder()

        for(i in 0 until digestBytes.size) {
            hexString.append(Integer.toHexString((0xFF and digestBytes[i].toInt())))
        }

        return hexString.toString()

    }

}

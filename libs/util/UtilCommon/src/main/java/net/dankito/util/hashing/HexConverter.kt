package net.dankito.util.hashing

import kotlin.experimental.and


class HexConverter {

    companion object {

        private val hexArray = "0123456789ABCDEF".toCharArray()


        fun byteArrayToHexString(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)

            for(j in bytes.indices) {
                // see http://kotlinlang.org/docs/reference/basic-types.html #Operations
                val v = bytes[j].and(0xFF.toByte()).toInt()
                hexChars[j * 2] = hexArray[v ushr(4)]
                hexChars[j * 2 + 1] = hexArray[v.and(0x0F)]
            }

            return String(hexChars)
        }
    }


    fun hexStringToByteArray(hexString: String): ByteArray {
        val bytes = ByteArray(hexString.length / 2)

        for(i in bytes.indices) {
            val index = i * 2
            val v = Integer.parseInt(hexString.substring(index, index + 2), 16)
            bytes[i] = v.toByte()
        }

        return bytes
    }

    @JvmOverloads fun byteArrayToHexStringViaStringFormat(bytes: ByteArray, bytesSeparator: String? = ""): String {
        val nonNullBytesSeparator = bytesSeparator ?: ""

        val stringBuilder = StringBuilder()

        for(singleByte in bytes) {
            stringBuilder.append(String.format("%02X" + nonNullBytesSeparator, singleByte))
        }

        stringBuilder.replace(stringBuilder.length - nonNullBytesSeparator.length, stringBuilder.length, "")

        return stringBuilder.toString()
    }

}

package net.dankito.utils.services.hashing


enum class HashAlgorithm private constructor(val algorithmName: String) {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA224("SHA-224"),
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512");


    override fun toString(): String {
        return algorithmName
    }

}

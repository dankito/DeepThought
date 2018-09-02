package net.dankito.data_access.network.communication.message

import net.dankito.utils.hashing.HashAlgorithm
import net.dankito.utils.hashing.HashService
import net.dankito.utils.hashing.IBase64Service
import java.util.*


class ChallengeHandler(private val base64Service: IBase64Service, private val hashService: HashService) {

    companion object {
        private val DEFAULT_COUNT_RETRIES = 2

        private val CHALLENGE_RESPONSE_HASH_ALGORITHM = HashAlgorithm.SHA512
    }


    private val random = Random(System.nanoTime())

    private val nonceToDeviceInfoMap: MutableMap<String, DeviceInfo> = HashMap()

    private val nonceToCorrectResponsesMap: MutableMap<String, String> = HashMap()

    private val nonceToCountRetriesMap: MutableMap<String, Int> = HashMap()


    fun createChallengeForDevice(deviceInfo: DeviceInfo): NonceToResponsePair {
        val nonce = UUID.randomUUID().toString()
        val correctResponse = createCorrectResponse()

        nonceToDeviceInfoMap.put(nonce, deviceInfo)

        nonceToCorrectResponsesMap.put(nonce, correctResponse)

        nonceToCountRetriesMap.put(nonce, DEFAULT_COUNT_RETRIES)

        return NonceToResponsePair(nonce, correctResponse)
    }

    private fun createCorrectResponse(): String {
        val response = random.nextInt(1000000)

        return String.format("%06d", response)
    }


    fun createChallengeResponse(nonce: String, enteredCode: String): String? {
        val challengeResponse = nonce + "-" + enteredCode

        try {
            val hashedChallengeResponse = hashService.hashStringToBytes(CHALLENGE_RESPONSE_HASH_ALGORITHM, challengeResponse)

            return base64Service.encode(hashedChallengeResponse)
        } catch (e: Exception) { /* should actually never occur */
        }

        return null
    }


    fun isResponseOk(nonce: String, base64EncodedChallengeResponse: String): Boolean {
        val isCorrectResponse = isCorrectResponse(nonce, base64EncodedChallengeResponse)

        if (isCorrectResponse) {
            nonceToCountRetriesMap.remove(nonce)
            nonceToCorrectResponsesMap.remove(nonce)
        }
        else {
            var countRetries = 0
            nonceToCountRetriesMap[nonce]?.let { retries ->
                countRetries = retries - 1
            }

            if(countRetries > 0) {
                nonceToCountRetriesMap.put(nonce, countRetries)
            }
            else {
                nonceToCountRetriesMap.remove(nonce)
            }
        }

        return isCorrectResponse
    }

    private fun isCorrectResponse(nonce: String, base64EncodedChallengeResponse: String): Boolean {
        // check if nonceToCorrectResponsesMap really contains nonce as otherwise (null, null) would be a correct response
        if (nonceToCorrectResponsesMap.containsKey(nonce)) {
            nonceToCorrectResponsesMap[nonce]?.let { correctResponse ->
                val correctChallengeResponse = createChallengeResponse(nonce, correctResponse)

                return base64EncodedChallengeResponse == correctChallengeResponse
            }
        }

        return false
    }

    fun getCountRetriesLeftForNonce(nonce: String): Int {
        nonceToCountRetriesMap[nonce]?.let { return it }

        return 0
    }

    fun getDeviceInfoForNonce(nonce: String): DeviceInfo? {
        return nonceToDeviceInfoMap[nonce]
    }

}

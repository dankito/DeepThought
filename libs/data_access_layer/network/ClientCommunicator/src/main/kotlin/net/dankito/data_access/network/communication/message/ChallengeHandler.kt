package net.dankito.data_access.network.communication.message

import net.dankito.utils.services.hashing.HashAlgorithm
import net.dankito.utils.services.hashing.HashService
import net.dankito.utils.services.hashing.IBase64Service
import java.util.*


class ChallengeHandler(protected var base64Service: IBase64Service) {

    companion object {
        protected val DEFAULT_COUNT_RETRIES = 2

        protected val CHALLENGE_RESPONSE_HASH_ALGORITHM = HashAlgorithm.SHA512
    }


    protected var hashService = HashService()

    protected var random = Random(System.nanoTime())

    protected var nonceToDeviceInfoMap: MutableMap<String, DeviceInfo> = HashMap()

    protected var nonceToCorrectResponsesMap: MutableMap<String, String> = HashMap()

    protected var nonceToCountRetriesMap: MutableMap<String, Int> = HashMap()


    fun createChallengeForDevice(deviceInfo: DeviceInfo): NonceToResponsePair {
        val nonce = UUID.randomUUID().toString()
        val correctResponse = createCorrectResponse()

        nonceToDeviceInfoMap.put(nonce, deviceInfo)

        nonceToCorrectResponsesMap.put(nonce, correctResponse)

        nonceToCountRetriesMap.put(nonce, DEFAULT_COUNT_RETRIES)

        return NonceToResponsePair(nonce, correctResponse)
    }

    protected fun createCorrectResponse(): String {
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


    fun isResponseOk(nonce: String, base64EncodeChallengeResponse: String): Boolean {
        val isCorrectResponse = isCorrectResponse(nonce, base64EncodeChallengeResponse)

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

    protected fun isCorrectResponse(nonce: String, base64EncodeChallengeResponse: String): Boolean {
        // check if nonceToCorrectResponsesMap really contains nonce as otherwise (null, null) would be a correct response
        if (nonceToCorrectResponsesMap.containsKey(nonce)) {
            nonceToCorrectResponsesMap[nonce]?.let { correctResponse ->
                val correctChallengeResponse = createChallengeResponse(nonce, correctResponse)

                return base64EncodeChallengeResponse == correctChallengeResponse
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

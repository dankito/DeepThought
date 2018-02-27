package net.dankito.synchronization.device.communication.message


data class NonceToResponsePair(val nonce: String, val correctResponse: String)

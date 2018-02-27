package net.dankito.util.network


data class SocketResult(val isSuccessful: Boolean, val error: Exception? = null, val receivedMessage: String? = null, val countBytesSend: Long? = null) {

    override fun toString(): String {
        if(isSuccessful) {
            receivedMessage?.let {
                return "Successful: " + it
            }

            return "Successful"

        }
        else {
            return "Error: " + error
        }
    }

}

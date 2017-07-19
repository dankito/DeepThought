package net.dankito.data_access.network.communication

import org.slf4j.LoggerFactory
import java.io.*
import java.net.Socket


open class SocketHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SocketHandler::class.java)
    }


    fun sendMessage(socket: Socket, message: ByteArray): SocketResult {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = BufferedInputStream(ByteArrayInputStream(message))
            outputStream = BufferedOutputStream(socket.getOutputStream())

            return sendMessage(inputStream, outputStream)
        } catch (e: Exception) {
            log.error("Could not send message to client " + socket.inetAddress, e)
            return SocketResult(false, e)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    log.warn("Could not close input stream", e)
                }

            }

            if (outputStream != null) {
                try {
                    outputStream.flush()
                } catch (e: Exception) {
                    log.warn("Could not flush output stream", e)
                }

                // do not close outputStream, otherwise socket gets closed as well
            }
        }
    }

    @Throws(IOException::class)
    internal fun sendMessage(inputStream: InputStream, outputStream: OutputStream): SocketResult {
        inputStream.copyTo(outputStream, CommunicationConfig.BUFFER_SIZE)

        outputStream.flush()

        return SocketResult(true)
    }


    fun receiveMessage(socket: Socket): SocketResult {
        try {
            val inputStream = BufferedInputStream(socket.getInputStream())

            // do not close inputStream, otherwise socket gets closed
            return receiveMessage(inputStream)
        } catch (e: Exception) {
            log.error("Could not receive response", e)

            return SocketResult(false, e)
        }

    }

    @Throws(IOException::class)
    protected fun receiveMessage(inputStream: InputStream): SocketResult {
        val buffer = ByteArray(CommunicationConfig.BUFFER_SIZE)
        val receivedMessageBytes = ArrayList<Byte>()

        var receivedChunkSize: Int
        var receivedMessageSize: Int = 0

        do {
            receivedChunkSize = inputStream.read(buffer, 0, buffer.size)
            receivedMessageSize += receivedChunkSize

            if(receivedChunkSize > 0) {
                receivedMessageBytes.addAll(buffer.take(receivedChunkSize))

                if(buffer[receivedChunkSize - 1] == CommunicationConfig.MESSAGE_END_CHAR.toByte()) {
                    break
                }
            }
        } while(receivedChunkSize > -1)

        if (receivedChunkSize > 0 && receivedChunkSize < CommunicationConfig.MAX_MESSAGE_SIZE) {
            val receivedMessage = String(receivedMessageBytes.toByteArray(), CommunicationConfig.MESSAGE_CHARSET)
            return SocketResult(true, receivedMessage = receivedMessage)
        }
        else {
            if (receivedChunkSize <= 0) {
                return SocketResult(false, Exception("Could not receive any bytes"))
            }
            else {
                return SocketResult(false, Exception("Received message exceeds max message length of " + CommunicationConfig.MAX_MESSAGE_SIZE))
            }
        }
    }

    fun closeSocket(socket: Socket?) {
        if (socket != null) {
            try {
                socket.close()
            } catch (e: Exception) {
                log.error("Could not close socket", e)
            }

        }
    }

}

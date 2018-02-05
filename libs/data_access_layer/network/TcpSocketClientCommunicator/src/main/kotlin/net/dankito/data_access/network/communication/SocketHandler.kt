package net.dankito.data_access.network.communication

import okio.Okio
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.Socket


open class SocketHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SocketHandler::class.java)
    }


    fun sendMessage(socket: Socket, message: ByteArray): SocketResult {
        return sendMessage(socket, ByteArrayInputStream(message), CommunicationConfig.MESSAGE_END_CHAR)
    }

    fun sendMessage(socket: Socket, inputStream: InputStream, messageEndChar: Char? = null): SocketResult {
        var countBytesSend = -1L

        try {
            val sink = Okio.buffer(Okio.sink(socket))
            val source = Okio.buffer(Okio.source(BufferedInputStream(inputStream)))

            countBytesSend = sink.writeAll(source)

            messageEndChar?.let {
                sink.writeUtf8(it.toString()) // to signal receiver that message ends here
            }

            sink.flush()

            source.close()

            return SocketResult(true, countBytesSend = countBytesSend)
        } catch(e: Exception) {
            log.error("Could not send message to ${socket.inetAddress}", e)

            return SocketResult(false, e, countBytesSend = countBytesSend)
        }
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

        if(receivedMessageSize > 0 && receivedMessageSize < CommunicationConfig.MAX_MESSAGE_SIZE) {
            val receivedMessage = String(receivedMessageBytes.toByteArray(), CommunicationConfig.MESSAGE_CHARSET)
            return SocketResult(true, receivedMessage = receivedMessage)
        }
        else {
            if(receivedMessageSize <= 0) {
                return SocketResult(false, Exception("Could not receive any bytes"))
            }
            else {
                return SocketResult(false, Exception("Received message exceeds max message length of " + CommunicationConfig.MAX_MESSAGE_SIZE))
            }
        }
    }

    fun closeSocket(socket: Socket?) {
        if(socket != null) {
            try {
                socket.close()
            } catch (e: Exception) {
                log.error("Could not close socket", e)
            }

        }
    }

}

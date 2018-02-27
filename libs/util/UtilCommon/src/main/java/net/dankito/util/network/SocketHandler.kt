package net.dankito.util.network

import okio.Okio
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.Socket
import java.nio.charset.Charset


open class SocketHandler {

    companion object {
        private val log = LoggerFactory.getLogger(SocketHandler::class.java)
    }


    @JvmOverloads
    open fun sendMessage(socket: Socket, message: ByteArray, messageEndChar: Char? = SocketHandlerDefaultConfig.MESSAGE_END_CHAR): SocketResult {
        return sendMessage(socket, ByteArrayInputStream(message), messageEndChar)
    }

    @JvmOverloads
    open fun sendMessage(socket: Socket, inputStream: InputStream, messageEndChar: Char? = SocketHandlerDefaultConfig.MESSAGE_END_CHAR): SocketResult {
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


    @JvmOverloads
    open fun receiveMessage(socket: Socket, bufferSize: Int = SocketHandlerDefaultConfig.BUFFER_SIZE,
                            messageCharset: Charset = SocketHandlerDefaultConfig.MESSAGE_CHARSET,
                            messageEndChar: Char = SocketHandlerDefaultConfig.MESSAGE_END_CHAR,
                            maxMessageSize: Int = SocketHandlerDefaultConfig.MAX_MESSAGE_SIZE): SocketResult {
        try {
            val inputStream = BufferedInputStream(socket.getInputStream())

            // do not close inputStream, otherwise socket gets closed
            return receiveMessage(inputStream, bufferSize, messageCharset, messageEndChar, maxMessageSize)
        } catch (e: Exception) {
            log.error("Could not receive response", e)

            return SocketResult(false, e)
        }

    }

    @Throws(IOException::class)
    protected open fun receiveMessage(inputStream: InputStream, bufferSize: Int, messageCharset: Charset,
                                 messageEndChar: Char, maxMessageSize: Int): SocketResult {
        val buffer = ByteArray(bufferSize)
        val receivedMessageBytes = ArrayList<Byte>()

        var receivedChunkSize: Int
        var receivedMessageSize: Int = 0

        do {
            receivedChunkSize = inputStream.read(buffer, 0, buffer.size)
            receivedMessageSize += receivedChunkSize

            if(receivedChunkSize > 0) {
                receivedMessageBytes.addAll(buffer.take(receivedChunkSize))

                if(buffer[receivedChunkSize - 1] == messageEndChar.toByte()) {
                    break
                }
            }
        } while(receivedChunkSize > -1)

        if(receivedMessageSize > 0 && receivedMessageSize < maxMessageSize) {
            val receivedMessage = String(receivedMessageBytes.toByteArray(), messageCharset)
            return SocketResult(true, receivedMessage = receivedMessage)
        }
        else {
            if(receivedMessageSize <= 0) {
                return SocketResult(false, Exception("Could not receive any bytes"))
            }
            else {
                return SocketResult(false, Exception("Received message exceeds max message length of " + maxMessageSize))
            }
        }
    }

    open fun closeSocket(socket: Socket?) {
        if(socket != null) {
            try {
                socket.close()
            } catch (e: Exception) {
                log.error("Could not close socket", e)
            }

        }
    }

}

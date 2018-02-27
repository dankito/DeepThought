package net.dankito.data_access.network.communication


import net.dankito.data_access.network.communication.message.IMessageHandler
import net.dankito.data_access.network.communication.message.Request
import net.dankito.data_access.network.communication.message.Response
import net.dankito.data_access.network.communication.message.ResponseErrorType
import net.dankito.util.IThreadPool
import net.dankito.util.network.SocketHandler
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException


class RequestReceiver(private var socketHandler: SocketHandler, private var messageHandler: IMessageHandler, private var messageSerializer: IMessageSerializer, private var threadPool: IThreadPool)
    : IRequestReceiver {

    private var receiverSocket: ServerSocket? = null

    private var receiverThread: Thread? = null


    override fun close() {
        try { receiverSocket?.close() } catch (e: Exception) { log.error("Could not close receiver socket", e) }
        receiverSocket = null

        try { receiverThread?.interrupt() } catch(e: Exception) { log.error("Could not stop receiver thread", e) }
        receiverThread = null
    }


    override fun start(desiredMessagesReceiverPort: Int, callback: RequestReceiverCallback) {
        createReceiverSocketAsync(desiredMessagesReceiverPort, callback)
    }

    private fun createReceiverSocketAsync(desiredPort: Int, callback: RequestReceiverCallback) {
        receiverThread = Thread(Runnable { createReceiverSocket(desiredPort, callback) })

        receiverThread?.start()
    }

    private fun createReceiverSocket(desiredPort: Int, callback: RequestReceiverCallback) {
        try {
            receiverSocket = ServerSocket(desiredPort)

            receiverSocketBoundToPort(desiredPort, callback)

            waitForArrivingRequests()
        } catch (e: Exception) {
            log.error("Could not bind receiverSocket to port " + desiredPort, e)
            if (e is BindException || e is IOException) {
                createReceiverSocket(desiredPort + 1, callback)
            } else {
                creatingReceiverSocketFailed(desiredPort, e, callback)
            }
        }

    }

    private fun creatingReceiverSocketFailed(port: Int, exception: Exception, callback: RequestReceiverCallback) {
        callback.started(this, false, port, exception)
    }

    private fun receiverSocketBoundToPort(port: Int, callback: RequestReceiverCallback) {
        callback.started(this, true, port, null)
    }

    private fun waitForArrivingRequests() {
        while (Thread.currentThread().isInterrupted == false && receiverSocket != null) {
            try {
                receiverSocket?.let { receiverSocket ->
                    val clientSocket = receiverSocket.accept()

                    receivedRequestAsync(clientSocket)
                } ?: break
            } catch (e: Exception) {
                if (e is InterruptedException || e is SocketException) {
                    break
                } else {
                    log.error("An error occurred accepting client request", e)
                }
            }

        }
    }

    internal fun receivedRequestAsync(clientSocket: Socket) {
        threadPool.runAsync { receivedRequest(clientSocket) }
    }

    private fun receivedRequest(clientSocket: Socket) {
        val socketResult = socketHandler.receiveMessage(clientSocket)

        if (socketResult.isSuccessful) {
            socketResult.receivedMessage?.let { receivedMessage ->
                receivedRequest(clientSocket, receivedMessage)
            }
        } else {
            // TODO: what to do in error case?

            socketHandler.closeSocket(clientSocket)
        }
    }

    private fun receivedRequest(clientSocket: Socket, requestString: String) {
        try {
            val request = messageSerializer.deserializeRequest(requestString)
            receivedRequest(clientSocket, request)
        } catch (e: Exception) {
            log.error("Could not deserialize request string " + requestString, e)
            dispatchResponseToRequest(clientSocket, null, Response<Any>(ResponseErrorType.DESERIALIZE_REQUEST, e))
        }

    }

    private fun receivedRequest(clientSocket: Socket, request: Request<*>) {
        messageHandler.handleReceivedRequest(request) { response -> dispatchResponseToRequest(clientSocket, request, response) }
    }


    private fun dispatchResponseToRequest(clientSocket: Socket, request: Request<*>?, response: Response<*>) {
        try {
            val serializedResponse = messageSerializer.serializeResponse(response)
            val (isSuccessful) = socketHandler.sendMessage(clientSocket, serializedResponse)

            if (isSuccessful) {

            }
            // TODO: what to do in error case?
        } catch (e: Exception) {
            log.error("Could not send response " + response + " to request " + request + " to client " + if (clientSocket != null) clientSocket.inetAddress else "", e)
        }

        socketHandler.closeSocket(clientSocket)
    }

    companion object {

        private val log = LoggerFactory.getLogger(RequestReceiver::class.java)
    }

}

package net.dankito.synchronization.device.messaging.tcp

import net.dankito.synchronization.device.messaging.message.Request
import net.dankito.synchronization.device.messaging.message.Response
import net.dankito.synchronization.device.messaging.message.ResponseErrorType
import net.dankito.util.IThreadPool
import net.dankito.util.network.SocketHandler
import org.slf4j.LoggerFactory
import java.net.Socket
import java.net.SocketAddress


class RequestSender(private var socketHandler: SocketHandler, private var messageSerializer: IMessageSerializer, private var threadPool: IThreadPool) : IRequestSender {

    companion object {
        private val log = LoggerFactory.getLogger(RequestSender::class.java)
    }


    override fun <TRequest, TResponse> sendRequestAndReceiveResponseAsync(destinationAddress: SocketAddress, request: Request<TRequest>,
                                                                          callback: (Response<TResponse>) -> Unit) {
        threadPool.runAsync { sendRequestAndReceiveResponse(destinationAddress, request, callback) }
    }


    private fun <TRequest, TResponse> sendRequestAndReceiveResponse(destinationAddress: SocketAddress, request: Request<TRequest>,
                                                callback: (Response<TResponse>) -> Unit) {
        val socket = Socket()

        try {
            socket.soTimeout = 0 // disables socket read() timeout
            socket.reuseAddress = true
            socket.connect(destinationAddress)

            if (sendRequest(socket, request, callback)) {
                receiveResponse(socket, request, callback)
            }
        } catch (e: Exception) {
            log.error("Could not send request to $destinationAddress for request $request", e)
            callback(Response<TResponse>(ResponseErrorType.SEND_REQUEST_TO_REMOTE, e))
        } finally {
            socketHandler.closeSocket(socket)
        }
    }

    private fun <TRequest, TResponse> sendRequest(socket: Socket, request: Request<TRequest>, callback: (Response<TResponse>) -> Unit): Boolean {
        try {
            val message = messageSerializer.serializeRequest(request) ?: return false

            val socketResult = socketHandler.sendMessage(socket, message)

            socketResult.error?.let { error ->
                callback(Response<TResponse>(ResponseErrorType.SEND_REQUEST_TO_REMOTE, error))
            }

            return socketResult.isSuccessful
        } catch (e: Exception) {
            log.error("Could not send request " + request + " to client " + if (socket != null) socket.inetAddress else "", e)
            callback(Response<TResponse>(ResponseErrorType.SERIALIZE_REQUEST, e))
        }

        return false
    }


    private fun <TRequest, TResponse> receiveResponse(socket: Socket, request: Request<TRequest>, callback: (Response<TResponse>) -> Unit) {
        val socketResult = socketHandler.receiveMessage(socket)

        socketResult.error?.let { error ->
            callback(Response(ResponseErrorType.RETRIEVE_RESPONSE, error))
        }
        socketResult.receivedMessage?.let { receivedMessage ->
            deserializeReceivedResponse(request.method, receivedMessage, callback)
        }
    }

    private fun <TResponse> deserializeReceivedResponse(methodName: String, responseString: String, callback: (Response<TResponse>) -> Unit) {
        val response = messageSerializer.deserializeResponse<TResponse>(methodName, responseString)
        callback(response)
    }

}

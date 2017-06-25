package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.callback.SendRequestCallback
import net.dankito.data_access.network.communication.message.Request
import net.dankito.data_access.network.communication.message.Response
import net.dankito.data_access.network.communication.message.ResponseErrorType
import net.dankito.utils.IThreadPool
import org.slf4j.LoggerFactory
import java.net.Socket
import java.net.SocketAddress


class RequestSender(private var socketHandler: SocketHandler, private var messageSerializer: IMessageSerializer, private var threadPool: IThreadPool) : IRequestSender {

    companion object {
        private val log = LoggerFactory.getLogger(RequestSender::class.java)
    }


    override fun <TRequest, TResponse> sendRequestAndReceiveResponseAsync(destinationAddress: SocketAddress, request: Request<TRequest>,
                                                                          callback: SendRequestCallback<TResponse>) {
        threadPool.runAsync { sendRequestAndReceiveResponse(destinationAddress, request, callback) }
    }


    private fun <TRequest, TResponse> sendRequestAndReceiveResponse(destinationAddress: SocketAddress, request: Request<TRequest>,
                                                callback: SendRequestCallback<TResponse>) {
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
            callback.done(Response<TResponse>(ResponseErrorType.SEND_REQUEST_TO_REMOTE, e))
        } finally {
            socketHandler.closeSocket(socket)
        }
    }

    private fun <TRequest, TResponse> sendRequest(socket: Socket, request: Request<TRequest>, callback: SendRequestCallback<TResponse>): Boolean {
        try {
            val message = messageSerializer.serializeRequest(request) ?: return false

            val socketResult = socketHandler.sendMessage(socket, message)

            socketResult.error?.let { error ->
                callback.done(Response<TResponse>(ResponseErrorType.SEND_REQUEST_TO_REMOTE, error))
            }

            return socketResult.isSuccessful
        } catch (e: Exception) {
            log.error("Could not send request " + request + " to client " + if (socket != null) socket.inetAddress else "", e)
            callback.done(Response<TResponse>(ResponseErrorType.SERIALIZE_REQUEST, e))
        }

        return false
    }


    private fun <TRequest, TResponse> receiveResponse(socket: Socket, request: Request<TRequest>, callback: SendRequestCallback<TResponse>) {
        val socketResult = socketHandler.receiveMessage(socket)

        socketResult.error?.let { error ->
            callback.done(Response(ResponseErrorType.RETRIEVE_RESPONSE, error))
        }
        socketResult.receivedMessage?.let { receivedMessage ->
            deserializeReceivedResponse(request.method, receivedMessage, callback)
        }
    }

    private fun <TResponse> deserializeReceivedResponse(methodName: String, responseString: String, callback: SendRequestCallback<TResponse>) {
        val response = messageSerializer.deserializeResponse<TResponse>(methodName, responseString)
        callback.done(response)
    }

}

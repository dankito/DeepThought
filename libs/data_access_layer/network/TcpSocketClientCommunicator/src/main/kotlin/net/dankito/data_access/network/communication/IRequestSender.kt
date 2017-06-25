package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.callback.SendRequestCallback
import net.dankito.data_access.network.communication.message.Request

import java.net.SocketAddress


interface IRequestSender {

    fun <TRequest, TResponse> sendRequestAndReceiveResponseAsync(destinationAddress: SocketAddress, request: Request<TRequest>, callback: SendRequestCallback<TResponse>)

}

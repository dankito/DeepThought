package net.dankito.synchronization.device.messaging.tcp

import net.dankito.synchronization.device.messaging.message.Request
import net.dankito.synchronization.device.messaging.message.Response
import java.net.SocketAddress


interface IRequestSender {

    fun <TRequest, TResponse> sendRequestAndReceiveResponseAsync(destinationAddress: SocketAddress, request: Request<TRequest>, callback: (Response<TResponse>) -> Unit)

}
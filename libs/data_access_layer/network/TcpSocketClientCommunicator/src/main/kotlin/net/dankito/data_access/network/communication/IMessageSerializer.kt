package net.dankito.data_access.network.communication

import net.dankito.synchronization.device.communication.message.Request
import net.dankito.synchronization.device.communication.message.Response


interface IMessageSerializer {

    @Throws(Exception::class)
    fun serializeRequest(request: Request<*>): ByteArray?

    @Throws(Exception::class)
    fun deserializeRequest(requestString: String): Request<*>

    @Throws(Exception::class)
    fun serializeResponse(response: Response<*>): ByteArray

    fun <TResponse> deserializeResponse(methodName: String, responseString: String): Response<TResponse>

}

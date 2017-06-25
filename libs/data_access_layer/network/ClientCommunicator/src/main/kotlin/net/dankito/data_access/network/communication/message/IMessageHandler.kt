package net.dankito.data_access.network.communication.message

import net.dankito.data_access.network.communication.callback.RequestHandlerCallback


interface IMessageHandler {

    fun handleReceivedRequest(request: Request<*>, callback: RequestHandlerCallback)

    @Throws(Exception::class)
    fun getRequestBodyClassForMethod(methodName: String): Class<*>?

    @Throws(Exception::class)
    fun getResponseBodyClassForMethod(methodName: String): Class<*>

}

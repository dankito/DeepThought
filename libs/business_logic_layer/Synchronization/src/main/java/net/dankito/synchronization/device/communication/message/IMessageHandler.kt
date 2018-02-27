package net.dankito.synchronization.device.communication.message


interface IMessageHandler {

    fun handleReceivedRequest(request: Request<*>, callback: (Response<out Any>) -> Unit)

    @Throws(Exception::class)
    fun getRequestBodyClassForMethod(methodName: String): Class<*>?

    @Throws(Exception::class)
    fun getResponseBodyClassForMethod(methodName: String): Class<*>

}

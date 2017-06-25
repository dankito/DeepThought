package net.dankito.data_access.network.communication.message


interface IMessageHandler {

    fun handleReceivedRequest(request: Request<*>, callback: (Response<out Any>) -> Unit)

    @Throws(Exception::class)
    fun getRequestBodyClassForMethod(methodName: String): Class<*>?

    @Throws(Exception::class)
    fun getResponseBodyClassForMethod(methodName: String): Class<*>

}

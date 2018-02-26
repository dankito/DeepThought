package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.message.IMessageHandler
import net.dankito.data_access.network.communication.message.Request
import net.dankito.data_access.network.communication.message.Response
import net.dankito.data_access.network.communication.message.ResponseErrorType
import net.dankito.util.serialization.ISerializer
import org.slf4j.LoggerFactory
import java.io.IOException


class JsonMessageSerializer(private val messageHandler: IMessageHandler, private val serializer: ISerializer) : IMessageSerializer {

    companion object {
        private val log = LoggerFactory.getLogger(JsonMessageSerializer::class.java)
    }


    @Throws(Exception::class)
    override fun serializeRequest(request: Request<*>): ByteArray? {
        var requestBodyString: String? = null
        request.body?.let { body ->
            requestBodyString = serializeObject(body)
        }

        val requestString = createRequestString(request.method, requestBodyString)
        return addMessageEndStringAndGetBytesFromString(requestString)
    }

    protected fun createRequestString(methodName: String, body: String?): String {
        var requestString = methodName + CommunicationConfig.METHOD_NAME_AND_BODY_SEPARATOR

        if(body != null) {
            requestString += body
        }

        return requestString
    }


    @Throws(Exception::class)
    override fun deserializeRequest(requestString: String): Request<*> {
        val requestParts = requestString.split(CommunicationConfig.METHOD_NAME_AND_BODY_SEPARATOR.toRegex(), 2).toTypedArray()

        val methodName = requestParts[0]
        var requestBody: Any? = null

        if (requestParts.size > 1) { // requestParts.length == 1 -> request without a body
            val requestBodyString = requestParts[1]
            requestBody = deserializeRequestBody(methodName, requestBodyString)
        }

        return Request(methodName, requestBody)
    }

    @Throws(Exception::class)
    protected fun deserializeRequestBody(methodName: String, requestBodyString: String): Any? {
        val requestBodyClass = messageHandler.getRequestBodyClassForMethod(methodName)

        if (requestBodyClass != null) {
            return deserializeObject(requestBodyString, requestBodyClass)
        }

        return null
    }


    @Throws(Exception::class)
    override fun serializeResponse(response: Response<*>): ByteArray {
        val serializedResponse = serializeObject(response)
        return addMessageEndStringAndGetBytesFromString(serializedResponse)
    }


    override fun <TResponse> deserializeResponse(methodName: String, responseString: String): Response<TResponse> {
        try {
            val responseBodyType = messageHandler.getResponseBodyClassForMethod(methodName)
            return deserializeObject(responseString, Response::class.java, responseBodyType as Class<Any>) as Response<TResponse>
        } catch (e: Exception) {
            log.error("Could not deserialize response " + responseString, e)
            return Response<TResponse>(ResponseErrorType.DESERIALIZE_RESPONSE, e)
        }

    }


    @Throws(Exception::class)
    protected fun serializeObject(`object`: Any): String {
        return serializer.serializeObject(`object`)
    }

    @Throws(IOException::class)
    protected fun <T> deserializeObject(serializedObject: String, objectClass: Class<T>, vararg genericParameterTypes: Class<Any>): T {
        return serializer.deserializeObject(serializedObject, objectClass, *genericParameterTypes)
    }

    protected fun addMessageEndStringAndGetBytesFromString(string: String): ByteArray {
        return getBytesFromString(string)
    }

    protected fun getBytesFromString(string: String): ByteArray {
        return string.toByteArray(CommunicationConfig.MESSAGE_CHARSET)
    }

}

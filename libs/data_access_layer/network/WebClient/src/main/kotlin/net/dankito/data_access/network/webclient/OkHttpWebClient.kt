package net.dankito.data_access.network.webclient

import com.squareup.okhttp.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import java.util.concurrent.TimeUnit


class OkHttpWebClient : IWebClient {

    companion object {
        protected val FORM_URL_ENCODED_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
        protected val JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")

        protected val DEFAULT_CONNECTION_TIMEOUT_MILLIS = 10000

        private val log = LoggerFactory.getLogger(OkHttpWebClient::class.java)
    }


    protected var cookieManager = CookieManager()

    // avoid creating several instances, should be singleton
    protected var client = OkHttpClient()


    init {
        client.followRedirects = true
        client.retryOnConnectionFailure = true

        client.cookieHandler = cookieManager
    }


    override fun get(parameters: RequestParameters): WebClientResponse {
        try {
            val request = createGetRequest(parameters)

            val response = executeRequest(parameters, request)

            return getResponse(parameters, response)
        } catch (e: Exception) {
            return getRequestFailed(parameters, e)
        }

    }

    override fun getAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit) {
        try {
            val request = createGetRequest(parameters)

            executeRequestAsync(parameters, request, callback)
        } catch (e: Exception) {
            asyncGetRequestFailed(parameters, e, callback)
        }

    }

    protected fun createGetRequest(parameters: RequestParameters): Request {
        val requestBuilder = Request.Builder()

        applyParameters(requestBuilder, parameters)

        return requestBuilder.build()
    }


    override fun post(parameters: RequestParameters): WebClientResponse {
        try {
            val request = createPostRequest(parameters)

            val response = executeRequest(parameters, request)

            return getResponse(parameters, response)
        } catch (e: Exception) {
            return postRequestFailed(parameters, e)
        }

    }

    override fun postAsync(parameters: RequestParameters, callback: (response: WebClientResponse) -> Unit) {
        try {
            val request = createPostRequest(parameters)

            executeRequestAsync(parameters, request, callback)
        } catch (e: Exception) {
            asyncPostRequestFailed(parameters, e, callback)
        }

    }

    protected fun createPostRequest(parameters: RequestParameters): Request {
        val requestBuilder = Request.Builder()

        setPostBody(requestBuilder, parameters)

        applyParameters(requestBuilder, parameters)

        return requestBuilder.build()
    }

    protected fun setPostBody(requestBuilder: Request.Builder, parameters: RequestParameters) {
        if (parameters.isBodySet()) {
            val mediaType = if (parameters.contentType === ContentType.JSON) JSON_MEDIA_TYPE else FORM_URL_ENCODED_MEDIA_TYPE
            val postBody = RequestBody.create(mediaType, parameters.body)

            requestBuilder.post(postBody)
        }
    }

    protected fun applyParameters(requestBuilder: Request.Builder, parameters: RequestParameters) {
        requestBuilder.url(parameters.url)

        if (parameters.isUserAgentSet()) {
            requestBuilder.header("User-Agent", parameters.userAgent)
        }

        if (parameters.isConnectionTimeoutSet()) {
            client.setConnectTimeout(parameters.connectionTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
        } else {
            client.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT_MILLIS.toLong(), TimeUnit.MILLISECONDS)
        }

        setCookieHandling(parameters)
    }

    private fun setCookieHandling(parameters: RequestParameters) {
        when (parameters.cookieHandling) {
            CookieHandling.ACCEPT_ALL, CookieHandling.ACCEPT_ALL_ONLY_FOR_THIS_CALL -> cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            CookieHandling.ACCEPT_ORIGINAL_SERVER, CookieHandling.ACCEPT_ORIGINAL_SERVER_ONLY_FOR_THIS_CALL -> cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
            else -> cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE)
        }
    }

    @Throws(Exception::class)
    protected fun executeRequest(parameters: RequestParameters, request: Request): Response {
        val response = client.newCall(request).execute()

        if (parameters.cookieHandling === CookieHandling.ACCEPT_ALL_ONLY_FOR_THIS_CALL || parameters.cookieHandling === CookieHandling.ACCEPT_ORIGINAL_SERVER_ONLY_FOR_THIS_CALL) {
            cookieManager.getCookieStore().removeAll()
        }

        if (response.isSuccessful === false && parameters.isCountConnectionRetriesSet()) {
            prepareConnectionRetry(parameters)
            return executeRequest(parameters, request)
        } else {
            return response
        }
    }

    protected fun executeRequestAsync(parameters: RequestParameters, request: Request, callback: (response: WebClientResponse) -> Unit) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request, e: IOException) {
                asyncRequestFailed(parameters, request, e, callback)
            }

            @Throws(IOException::class)
            override fun onResponse(response: Response) {
                callback(getResponse(parameters, response))
            }
        })
    }

    protected fun getRequestFailed(parameters: RequestParameters, e: Exception): WebClientResponse {
        if (shouldRetryConnection(parameters, e)) {
            prepareConnectionRetry(parameters)
            return get(parameters)
        } else {
            log.error("Could not request url " + parameters.url, e)
            return WebClientResponse(false, e)
        }
    }

    protected fun asyncGetRequestFailed(parameters: RequestParameters, e: Exception, callback: (response: WebClientResponse) -> Unit) {
        if (shouldRetryConnection(parameters, e)) {
            prepareConnectionRetry(parameters)
            getAsync(parameters, callback)
        } else {
            callback(WebClientResponse(false, e))
        }
    }

    protected fun postRequestFailed(parameters: RequestParameters, e: Exception): WebClientResponse {
        if (shouldRetryConnection(parameters, e)) {
            prepareConnectionRetry(parameters)
            return post(parameters)
        } else {
            return WebClientResponse(false, e)
        }
    }

    protected fun asyncPostRequestFailed(parameters: RequestParameters, e: Exception, callback: (response: WebClientResponse) -> Unit) {
        if (shouldRetryConnection(parameters, e)) {
            prepareConnectionRetry(parameters)
            postAsync(parameters, callback)
        } else {
            callback(WebClientResponse(false, e))
        }
    }

    protected fun asyncRequestFailed(parameters: RequestParameters, request: Request, e: Exception, callback: (response: WebClientResponse) -> Unit) {
        if (shouldRetryConnection(parameters, e)) {
            prepareConnectionRetry(parameters)
            executeRequestAsync(parameters, request, callback)
        } else {
            log.error("Failure on Request to " + request.urlString(), e)
            callback(WebClientResponse(false, e))
        }
    }

    protected fun prepareConnectionRetry(parameters: RequestParameters) {
        parameters.decrementCountConnectionRetries()
        log.info("Going to retry to connect to " + parameters.url + " (count tries left: " + parameters.countConnectionRetries + ")")
    }

    protected fun shouldRetryConnection(parameters: RequestParameters, e: Exception): Boolean {
        return parameters.isCountConnectionRetriesSet() && isConnectionException(e)
    }

    protected fun isConnectionException(e: Exception): Boolean {
        val errorMessage = e.message?.toLowerCase() ?: ""
        return errorMessage.contains("timeout") || errorMessage.contains("failed to connect")
    }

    @Throws(IOException::class)
    protected fun getResponse(parameters: RequestParameters, response: Response): WebClientResponse {
        if (parameters.hasStringResponse) {
            return WebClientResponse(true, body = response.body().string())
        } else {
            return streamBinaryResponse(parameters, response)
        }
    }

    protected fun streamBinaryResponse(parameters: RequestParameters, response: Response): WebClientResponse {
        var inputStream: InputStream? = null
        try {
            inputStream = response.body().byteStream()

            val buffer = ByteArray(parameters.downloadBufferSize)
            var downloaded: Long = 0
            val contentLength = response.body().contentLength()

            publishProgress(parameters, ByteArray(0), 0L, contentLength)
            while (true) {
                val read = inputStream!!.read(buffer)
                if (read == -1) {
                    break
                }

                downloaded += read.toLong()

                publishProgress(parameters, buffer, downloaded, contentLength, read)

                if (isCancelled(parameters)) {
                    return WebClientResponse(false)
                }
            }

            return WebClientResponse(true)
        } catch (e: IOException) {
            log.error("Could not download binary Response for Url " + parameters.url, e)
            return WebClientResponse(false, e)
        } finally {
            inputStream?.let { try { it.close() } catch (ignored: Exception) { } }
        }
    }

    protected fun isCancelled(parameters: RequestParameters): Boolean {
        return false // TODO: implement mechanism to abort download
    }

    protected fun publishProgress(parameters: RequestParameters, buffer: ByteArray, downloaded: Long, contentLength: Long, read: Int) {
        var downloadedData = buffer

        if (read < parameters.downloadBufferSize) {
            downloadedData = Arrays.copyOfRange(buffer, 0, read)
        }

        publishProgress(parameters, downloadedData, downloaded, contentLength)
    }

    protected fun publishProgress(parameters: RequestParameters, downloadedChunk: ByteArray, currentlyDownloaded: Long, total: Long) {
        val progressListener = parameters.downloadProgressListener

        if (progressListener != null) {
            val progress = if (total <= 0) java.lang.Float.NaN else currentlyDownloaded / total.toFloat()
            progressListener(progress, downloadedChunk)
        }
    }

}
package net.dankito.webclient


data class RequestParameters(val url: String, var body: String? = null, var contentType: ContentType = ContentType.FORM_URL_ENCODED,
                             var userAgent: String? = null, var cookieHandling: CookieHandling = CookieHandling.ACCEPT_NONE,
                             var connectionTimeoutMillis: Int = 0, var countConnectionRetries: Int = 0, var hasStringResponse: Boolean = true,
                             var downloadBufferSize: Int = RequestParameters.DEFAULT_DOWNLOAD_BUFFER_SIZE,
                             var downloadProgressListener: ((progress: Float, downloadedChunk: ByteArray) -> Unit)? = null) {

    companion object {
        const val DEFAULT_DOWNLOAD_BUFFER_SIZE = 8 * 1024
    }


    fun isBodySet(): Boolean {
        return !body.isNullOrBlank()
    }

    fun isUserAgentSet(): Boolean {
        return !userAgent.isNullOrBlank()
    }

    fun isConnectionTimeoutSet(): Boolean {
        return connectionTimeoutMillis > 0
    }

    fun isCountConnectionRetriesSet(): Boolean {
        return countConnectionRetries > 0
    }

    fun decrementCountConnectionRetries() {
        this.countConnectionRetries--
    }

}
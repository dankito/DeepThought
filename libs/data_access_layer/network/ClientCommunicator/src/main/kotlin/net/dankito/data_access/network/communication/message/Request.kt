package net.dankito.data_access.network.communication.message


open class Request<T>(val method: String, val body: T? = null) {

    val isBodySet: Boolean
        get() = body != null


    override fun toString(): String {
        return method + ": " + body
    }

}

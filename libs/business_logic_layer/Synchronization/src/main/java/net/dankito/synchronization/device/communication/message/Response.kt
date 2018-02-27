package net.dankito.synchronization.device.communication.message

class Response<T> {

    var isCouldHandleMessage: Boolean = false
        protected set

    var errorType: ResponseErrorType = ResponseErrorType.NONE
        private set

    var error: Exception? = null
        private set

    var body: T? = null
        private set


    protected constructor() { // for Jackson

    }

    constructor(errorType: ResponseErrorType, error: Exception) {
        this.isCouldHandleMessage = false
        this.errorType = errorType
        this.error = error
    }

    constructor(body: T) {
        this.isCouldHandleMessage = true
        this.body = body
    }

    constructor(couldHandleMessage: Boolean, errorType: ResponseErrorType, error: Exception, body: T) {
        this.isCouldHandleMessage = couldHandleMessage
        this.errorType = errorType
        this.error = error
        this.body = body
    }


    override fun toString(): String {
        if (isCouldHandleMessage) {
            return "Success: " + body
        } else {
            return errorType.toString() + ": " + error
        }
    }

}

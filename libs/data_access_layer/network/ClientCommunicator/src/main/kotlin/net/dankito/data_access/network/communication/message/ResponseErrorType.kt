package net.dankito.data_access.network.communication.message


enum class ResponseErrorType {

    NONE,
    SERIALIZE_REQUEST,
    SEND_REQUEST_TO_REMOTE,
    DESERIALIZE_REQUEST,
    RETRIEVE_RESPONSE,
    DESERIALIZE_RESPONSE

}

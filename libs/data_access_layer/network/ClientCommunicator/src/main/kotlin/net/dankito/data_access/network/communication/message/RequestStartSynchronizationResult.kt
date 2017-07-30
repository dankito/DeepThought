package net.dankito.data_access.network.communication.message


enum class RequestStartSynchronizationResult {

    ALLOWED,
    COULD_NOT_START_LISTENER,
    DO_NOT_KNOW_YOU,
    INCOMPATIBLE_VERSION,
    DENIED

}

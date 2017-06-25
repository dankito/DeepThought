package net.dankito.data_access.network.communication


class CommunicatorConfig {

    companion object {

        const val DEFAULT_MESSAGES_RECEIVER_PORT = 32789


        const val GET_DEVICE_INFO_METHOD_NAME = "GetDeviceInfo"

        const val REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME = "RequestPermitSynchronization"

        const val RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME = "ResponseToSynchronizationPermittingChallenge"

        const val REQUEST_START_SYNCHRONIZATION_METHOD_NAME = "RequestStartSynchronization"

    }

}

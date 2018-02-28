package net.dankito.synchronization.device.messaging

import java.nio.charset.Charset


class MessengerConfig {

    companion object {

        const val DEFAULT_MESSAGES_RECEIVER_PORT = 32789


        const val METHOD_NAME_AND_BODY_SEPARATOR = ":"

        const val MESSAGE_CHARSET_NAME = "UTF-8"

        val MESSAGE_CHARSET = Charset.forName(MESSAGE_CHARSET_NAME)


        const val GET_DEVICE_INFO_METHOD_NAME = "GetDeviceInfo"

        const val REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME = "RequestPermitSynchronization"

        const val RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME = "ResponseToSynchronizationPermittingChallenge"

        const val REQUEST_START_SYNCHRONIZATION_METHOD_NAME = "RequestStartSynchronization"

    }

}

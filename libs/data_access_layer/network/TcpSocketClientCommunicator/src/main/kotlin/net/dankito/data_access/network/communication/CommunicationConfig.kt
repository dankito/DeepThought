package net.dankito.data_access.network.communication

import java.nio.charset.Charset


class CommunicationConfig {

    companion object {

        const val DefaultDeviceDiscoveryMessagePrefix = "DeepThought"


        const val METHOD_NAME_AND_BODY_SEPARATOR = ":"

        const val MESSAGE_CHARSET_NAME = "UTF-8"

        val MESSAGE_CHARSET = Charset.forName(MESSAGE_CHARSET_NAME)

    }

}

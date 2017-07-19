package net.dankito.data_access.network.communication

import java.nio.charset.Charset


class CommunicationConfig {

    companion object {

        const val MAX_MESSAGE_SIZE = 2 * 1024 * 1024

        const val BUFFER_SIZE = 1 * 1024

        const val MESSAGE_CHARSET_NAME = "UTF-8"

        val MESSAGE_CHARSET = Charset.forName(MESSAGE_CHARSET_NAME)

        const val METHOD_NAME_AND_BODY_SEPARATOR = ":"

        const val MESSAGE_END_CHAR = '\n'

    }

}

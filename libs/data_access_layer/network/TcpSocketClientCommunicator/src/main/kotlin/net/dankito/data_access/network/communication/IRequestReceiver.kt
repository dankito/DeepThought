package net.dankito.data_access.network.communication

interface IRequestReceiver {

    fun start(desiredMessagesReceiverPort: Int, callback: RequestReceiverCallback)

    fun close()

}

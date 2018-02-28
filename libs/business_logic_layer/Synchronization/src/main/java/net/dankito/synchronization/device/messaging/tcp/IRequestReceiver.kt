package net.dankito.synchronization.device.messaging.tcp

interface IRequestReceiver {

    fun start(desiredMessagesReceiverPort: Int, callback: RequestReceiverCallback)

    fun close()

}

package net.dankito.synchronization.device.messaging.tcp


interface RequestReceiverCallback {

    fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?)

}

package net.dankito.data_access.network.communication.callback


interface ClientCommunicatorListener {

    fun started(couldStartMessagesReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?)

}

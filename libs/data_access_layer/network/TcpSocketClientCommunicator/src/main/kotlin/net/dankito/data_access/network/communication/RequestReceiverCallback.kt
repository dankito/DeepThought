package net.dankito.data_access.network.communication


interface RequestReceiverCallback {

    fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?)

}

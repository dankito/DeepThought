package net.dankito.deepthought.communication

import net.dankito.data_access.network.communication.CommunicatorConfig
import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.callback.ClientCommunicatorListener
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.service.synchronization.IConnectedDevicesService


class CommunicationManager(private var connectedDevicesService: IConnectedDevicesService, private var clientCommunicator: IClientCommunicator, private var networkSettings: INetworkSettings) : ICommunicationManager {

    override fun startAsync() {
        clientCommunicator.start(CommunicatorConfig.DEFAULT_MESSAGES_RECEIVER_PORT, object : ClientCommunicatorListener {
            override fun started(couldStartMessagesReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                if (couldStartMessagesReceiver) {
                    successfullyStartedClientCommunicator(messagesReceiverPort)
                }
                else {
                    startException?.let { startException ->
                        startingClientCommunicatorFailed(startException)
                    }
                }
            }
        })
    }

    override fun stop() {
        connectedDevicesService.stop()

        clientCommunicator.stop()
    }


    private fun startingClientCommunicatorFailed(startException: Exception) {
        // TODO: what to do in error case?
    }

    private fun successfullyStartedClientCommunicator(messagesReceiverPort: Int) {
        networkSettings.messagePort = messagesReceiverPort

        connectedDevicesService.start()
    }
}

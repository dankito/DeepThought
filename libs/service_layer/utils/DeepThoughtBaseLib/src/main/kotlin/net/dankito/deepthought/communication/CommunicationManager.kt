package net.dankito.deepthought.communication

import net.dankito.synchronization.device.messaging.MessengerConfig
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.service.synchronization.IConnectedDevicesService
import net.dankito.synchronization.database.sync.ISyncManager
import java.util.*


class CommunicationManager(private val connectedDevicesService: IConnectedDevicesService, private val syncManager: ISyncManager, private val messenger: IMessenger,
                           private val networkSettings: NetworkSettings) : ICommunicationManager {

    override fun startAsync() {
        messenger.start(MessengerConfig.DEFAULT_MESSAGES_RECEIVER_PORT) { couldStartMessagesReceiver, messagesReceiverPort, startException ->
            if (couldStartMessagesReceiver) {
                successfullyStartedClientCommunicator(messagesReceiverPort)
            }
            else {
                startException?.let {
                    startingClientCommunicatorFailed(startException)
                }
            }
        }
    }

    override fun stop() {
        connectedDevicesService.stop()

        messenger.stop()

        syncManager.stop()
    }


    private fun startingClientCommunicatorFailed(startException: Exception) {
        // TODO: what to do in error case?
    }

    private fun successfullyStartedClientCommunicator(messagesReceiverPort: Int) {
        networkSettings.messagePort = messagesReceiverPort

        val random = Random(System.nanoTime())

        val desiredSynchronizationPort = messagesReceiverPort + random.nextInt(400)
        val desiredBasicDataSynchronizationPort = desiredSynchronizationPort + random.nextInt(700)

        syncManager.startAsync(desiredSynchronizationPort, desiredBasicDataSynchronizationPort) {
            successfullyStartedSyncManager(it)
        }
    }

    private fun successfullyStartedSyncManager(basicDataSyncPort: Int) {
        connectedDevicesService.start()
    }

}

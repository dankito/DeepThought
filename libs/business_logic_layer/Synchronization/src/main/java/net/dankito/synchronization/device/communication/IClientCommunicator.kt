package net.dankito.synchronization.device.communication

import net.dankito.synchronization.device.communication.message.*
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.SyncInfo
import java.net.SocketAddress


interface IClientCommunicator {

    fun start(desiredCommunicatorPort: Int, startedCallback: (couldStartMessagesReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) -> Unit)

    fun stop()

    fun getDeviceInfo(destinationAddress: SocketAddress, callback: (Response<DeviceInfo>) -> Unit)

    fun requestPermitSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit)

    fun respondToSynchronizationPermittingChallenge(remoteDevice: DiscoveredDevice, nonce: String, challengeResponse: String, syncInfo: SyncInfo,
                                                    callback: (Response<RespondToSynchronizationPermittingChallengeResponseBody>) -> Unit)

    fun requestStartSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit)


    fun addRemoteRequestedToStartSynchronizationListener(listener: (DiscoveredDevice) -> Unit)

}

package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.message.*
import net.dankito.deepthought.model.DiscoveredDevice
import java.net.SocketAddress


interface IClientCommunicator {

    fun start(desiredCommunicatorPort: Int, startedCallback: (couldStartMessagesReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) -> Unit)

    fun stop()

    fun getDeviceInfo(destinationAddress: SocketAddress, callback: (Response<DeviceInfo>) -> Unit)

    fun requestPermitSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit)

    fun respondToSynchronizationPermittingChallenge(remoteDevice: DiscoveredDevice, nonce: String, challengeResponse: String, callback: (Response<RespondToSynchronizationPermittingChallengeResponseBody>) -> Unit)

    fun requestStartSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit)

}

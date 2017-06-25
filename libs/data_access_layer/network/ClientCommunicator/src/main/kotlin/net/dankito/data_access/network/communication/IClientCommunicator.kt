package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.callback.ClientCommunicatorListener
import net.dankito.data_access.network.communication.callback.SendRequestCallback
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.data_access.network.communication.message.RequestPermitSynchronizationResponseBody
import net.dankito.data_access.network.communication.message.RequestStartSynchronizationResponseBody
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResponseBody
import net.dankito.deepthought.model.DiscoveredDevice

import java.net.SocketAddress


interface IClientCommunicator {

    fun start(desiredCommunicatorPort: Int, listener: ClientCommunicatorListener)

    fun stop()

    fun getDeviceInfo(destinationAddress: SocketAddress, callback: SendRequestCallback<DeviceInfo>)

    fun requestPermitSynchronization(remoteDevice: DiscoveredDevice, callback: SendRequestCallback<RequestPermitSynchronizationResponseBody>)

    fun respondToSynchronizationPermittingChallenge(remoteDevice: DiscoveredDevice, nonce: String, challengeResponse: String, callback: SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody>)

    fun requestStartSynchronization(remoteDevice: DiscoveredDevice, callback: SendRequestCallback<RequestStartSynchronizationResponseBody>)

}

package net.dankito.synchronization.device.messaging.tcp

import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.MessengerConfig
import net.dankito.synchronization.device.messaging.callback.IDeviceRegistrationHandler
import net.dankito.synchronization.device.messaging.message.*
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.synchronization.model.SyncInfo
import net.dankito.util.IThreadPool
import net.dankito.util.hashing.HashService
import net.dankito.util.hashing.IBase64Service
import net.dankito.util.network.SocketHandler
import net.dankito.util.serialization.ISerializer
import java.net.InetSocketAddress
import java.net.SocketAddress


class PlainTcpMessenger(private val networkSettings: NetworkSettings, registrationHandler: IDeviceRegistrationHandler, entityManager: IEntityManager,
                        serializer: ISerializer, base64Service: IBase64Service, hashService: HashService, threadPool: IThreadPool) : IMessenger {

    private val requestSender: IRequestSender

    private val requestReceiver: IRequestReceiver

    private val challengeHandler: ChallengeHandler = ChallengeHandler(base64Service, hashService)

    private val messageHandlerConfig: MessageHandlerConfig


    init {
        messageHandlerConfig = MessageHandlerConfig(entityManager, networkSettings, challengeHandler, registrationHandler)

        val socketHandler = SocketHandler()
        val messageHandler = MessageHandler(messageHandlerConfig)
        val messageSerializer = JsonMessageSerializer(messageHandler, serializer)

        this.requestSender = RequestSender(socketHandler, messageSerializer, threadPool)

        this.requestReceiver = RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool)
    }


    override fun start(desiredCommunicatorPort: Int, startedCallback: (couldStartMessagesReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) -> Unit) {
        requestReceiver.start(desiredCommunicatorPort, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                startedCallback(couldStartReceiver, messagesReceiverPort, startException)
            }
        })
    }

    override fun stop() {
        requestReceiver.close()
    }


    override fun getDeviceInfo(destinationAddress: SocketAddress, callback: (Response<DeviceInfo>) -> Unit) {
        requestSender.sendRequestAndReceiveResponseAsync<Any, DeviceInfo>(destinationAddress,
                Request<Any>(MessengerConfig.GET_DEVICE_INFO_METHOD_NAME)) { callback(it) }
    }


    override fun requestPermitSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit) {
        networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice)

        val request = Request(MessengerConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME,
                DeviceInfo.fromNetworkSettings(networkSettings))

        requestSender.sendRequestAndReceiveResponseAsync<DeviceInfo, RequestPermitSynchronizationResponseBody>(getSocketAddressFromDevice(remoteDevice), request) { response ->
                response.body?.let { responseBody: RequestPermitSynchronizationResponseBody ->
                    if (response.isCouldHandleMessage == false || responseBody.result !== RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
                        networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)
                    }
                }

                callback(response)
            }
    }

    override fun respondToSynchronizationPermittingChallenge(remoteDevice: DiscoveredDevice, nonce: String, challengeResponse: String, syncInfo: SyncInfo,
                                                             callback: (Response<RespondToSynchronizationPermittingChallengeResponseBody>) -> Unit) {
        challengeHandler.createChallengeResponse(nonce, challengeResponse)?.let { challengeResponse ->
            val request = Request<RespondToSynchronizationPermittingChallengeRequestBody>(MessengerConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME,
                    RespondToSynchronizationPermittingChallengeRequestBody(nonce, challengeResponse, syncInfo, networkSettings.synchronizationPort, networkSettings.fileSynchronizationPort))

            requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request) { response: Response<RespondToSynchronizationPermittingChallengeResponseBody> ->
                handleRespondToSynchronizationPermittingChallengeResponse(remoteDevice, response)

                callback(response)
            }
        }
    }

    private fun handleRespondToSynchronizationPermittingChallengeResponse(remoteDevice: DiscoveredDevice, response: Response<RespondToSynchronizationPermittingChallengeResponseBody>) {
        response.body?.let { body ->
            val result = body.result

            if(body.result !== RespondToSynchronizationPermittingChallengeResult.WRONG_CODE) {
                networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)
            }

            if(result == RespondToSynchronizationPermittingChallengeResult.ALLOWED) {
                remoteDevice.synchronizationPort = body.synchronizationPort
                remoteDevice.fileSynchronizationPort = body.fileSynchronizationPort
            }
        }
    }


    override fun requestStartSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit) {
        networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice)

        val request = Request(MessengerConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME,
                RequestStartSynchronizationRequestBody(DeviceInfo.fromNetworkSettings(networkSettings), networkSettings.synchronizationPort, networkSettings.fileSynchronizationPort))

        requestSender.sendRequestAndReceiveResponseAsync<RequestStartSynchronizationRequestBody, RequestStartSynchronizationResponseBody>(
                getSocketAddressFromDevice(remoteDevice), request) { response ->
            networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)

            callback(response)
        }
    }


    private fun getSocketAddressFromDevice(device: DiscoveredDevice): SocketAddress {
        return InetSocketAddress(device.address, device.messagesPort)
    }


    override fun addRemoteRequestedToStartSynchronizationListener(listener: (DiscoveredDevice) -> Unit) {
        messageHandlerConfig.addRemoteRequestedToStartSynchronizationListener(listener)
    }

}

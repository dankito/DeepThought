package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.*
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.hashing.IBase64Service
import java.net.InetSocketAddress
import java.net.SocketAddress


class TcpSocketClientCommunicator(private val networkSettings: INetworkSettings, registrationHandler: IDeviceRegistrationHandler, base64Service: IBase64Service,
                                  threadPool: IThreadPool) : IClientCommunicator {

    private lateinit var requestSender: IRequestSender

    private lateinit var requestReceiver: IRequestReceiver

    private lateinit var challengeHandler: ChallengeHandler


    init {
        setupDependencies(networkSettings, registrationHandler, base64Service, threadPool)
    }

    private fun setupDependencies(networkSettings: INetworkSettings, registrationHandler: IDeviceRegistrationHandler, base64Service: IBase64Service, threadPool: IThreadPool) {
        this.challengeHandler = ChallengeHandler(base64Service)

        val messageHandlerConfig = MessageHandlerConfig(networkSettings, challengeHandler, registrationHandler)

        val socketHandler = SocketHandler()
        val messageHandler = MessageHandler(messageHandlerConfig)
        val messageSerializer = JsonMessageSerializer(messageHandler)

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
                Request<Any>(CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME)) { callback(it) }
    }


    override fun requestPermitSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit) {
        networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice)

        val request = Request(CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME,
                DeviceInfo.fromDevice(networkSettings.localHostDevice))

        requestSender.sendRequestAndReceiveResponseAsync<DeviceInfo, RequestPermitSynchronizationResponseBody>(getSocketAddressFromDevice(remoteDevice), request) { response ->
                response.body?.let { responseBody ->
                    if (response.isCouldHandleMessage == false || responseBody.result !== RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
                        networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)
                    }
                }

                callback(response)
            }
    }

    override fun respondToSynchronizationPermittingChallenge(remoteDevice: DiscoveredDevice, nonce: String, challengeResponse: String,
                                                             callback: (Response<RespondToSynchronizationPermittingChallengeResponseBody>) -> Unit) {
        challengeHandler.createChallengeResponse(nonce, challengeResponse)?.let { challengeResponse ->
            val request = Request<RespondToSynchronizationPermittingChallengeRequestBody>(CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME,
                    RespondToSynchronizationPermittingChallengeRequestBody(nonce, challengeResponse, networkSettings.synchronizationPort))

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

            if(result === RespondToSynchronizationPermittingChallengeResult.ALLOWED) {
                remoteDevice.synchronizationPort = body.synchronizationPort
            }
        }
    }


    override fun requestStartSynchronization(remoteDevice: DiscoveredDevice, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit) {
        networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice)

        val request = Request(CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME,
                RequestStartSynchronizationRequestBody(networkSettings.localHostDevice.uniqueDeviceId, networkSettings.synchronizationPort))

        requestSender.sendRequestAndReceiveResponseAsync<RequestStartSynchronizationRequestBody, RequestStartSynchronizationResponseBody>(
                getSocketAddressFromDevice(remoteDevice), request) { response ->
            networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)

            callback(response)
        }
    }


    private fun getSocketAddressFromDevice(device: DiscoveredDevice): SocketAddress {
        return InetSocketAddress(device.address, device.messagesPort)
    }

}

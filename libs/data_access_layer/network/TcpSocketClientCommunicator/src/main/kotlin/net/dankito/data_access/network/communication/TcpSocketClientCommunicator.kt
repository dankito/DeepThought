package net.dankito.data_access.network.communication

import net.dankito.data_access.network.communication.callback.ClientCommunicatorListener
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.data_access.network.communication.callback.SendRequestCallback
import net.dankito.data_access.network.communication.message.*
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.utils.IThreadPool
import net.dankito.utils.services.hashing.IBase64Service
import java.net.InetSocketAddress
import java.net.SocketAddress


class TcpSocketClientCommunicator(private val networkSettings: INetworkSettings, permissionHandler: IsSynchronizationPermittedHandler, base64Service: IBase64Service,
                                  threadPool: IThreadPool) : IClientCommunicator {

    private lateinit var requestSender: IRequestSender

    private lateinit var requestReceiver: IRequestReceiver

    private lateinit var challengeHandler: ChallengeHandler


    init {
        setupDependencies(networkSettings, permissionHandler, base64Service, threadPool)
    }

    private fun setupDependencies(networkSettings: INetworkSettings, permissionHandler: IsSynchronizationPermittedHandler, base64Service: IBase64Service, threadPool: IThreadPool) {
        this.challengeHandler = ChallengeHandler(base64Service)

        val messageHandlerConfig = MessageHandlerConfig(networkSettings, challengeHandler, permissionHandler)

        val socketHandler = SocketHandler()
        val messageHandler = MessageHandler(messageHandlerConfig)
        val messageSerializer = JsonMessageSerializer(messageHandler)

        this.requestSender = RequestSender(socketHandler, messageSerializer, threadPool)

        this.requestReceiver = RequestReceiver(socketHandler, messageHandler, messageSerializer, threadPool)
    }


    override fun start(desiredCommunicatorPort: Int, listener: ClientCommunicatorListener) {
        requestReceiver.start(desiredCommunicatorPort, object : RequestReceiverCallback {
            override fun started(requestReceiver: IRequestReceiver, couldStartReceiver: Boolean, messagesReceiverPort: Int, startException: Exception?) {
                listener.started(couldStartReceiver, messagesReceiverPort, startException)
            }
        })
    }

    override fun stop() {
        requestReceiver.close()
    }


    override fun getDeviceInfo(destinationAddress: SocketAddress, callback: SendRequestCallback<DeviceInfo>) {
        requestSender.sendRequestAndReceiveResponseAsync<Any, DeviceInfo>(destinationAddress,
                Request<Any>(CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME), object : SendRequestCallback<DeviceInfo> {
            override fun done(response: Response<DeviceInfo>) {
                callback.done(response)
            }
        })
    }


    override fun requestPermitSynchronization(remoteDevice: DiscoveredDevice, callback: SendRequestCallback<RequestPermitSynchronizationResponseBody>) {
        networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice)

        val request = Request(CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME,
                DeviceInfo.fromDevice(networkSettings.localHostDevice))

        requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
                object : SendRequestCallback<RequestPermitSynchronizationResponseBody> {
                    override fun done(response: Response<RequestPermitSynchronizationResponseBody>) {
                        if (response.isCouldHandleMessage == false || response.body!!.result !== RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
                            networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)
                        }

                        callback.done(response)
                    }
                })
    }

    override fun respondToSynchronizationPermittingChallenge(remoteDevice: DiscoveredDevice, nonce: String, challengeResponse: String,
                                                             callback: SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody>) {
        challengeHandler.createChallengeResponse(nonce, challengeResponse)?.let { challengeResponse ->
            val request = Request(CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME,
                    RespondToSynchronizationPermittingChallengeRequestBody(nonce, challengeResponse, networkSettings.synchronizationPort))

            requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
                    object : SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody> {
                        override fun done(response: Response<RespondToSynchronizationPermittingChallengeResponseBody>) {
                            handleRespondToSynchronizationPermittingChallengeResponse(remoteDevice, response)

                            callback.done(response)
                        }
                    })
        }
    }

    private fun handleRespondToSynchronizationPermittingChallengeResponse(remoteDevice: DiscoveredDevice, response: Response<RespondToSynchronizationPermittingChallengeResponseBody>) {
        if (response.isCouldHandleMessage) {
            val result = response.body!!.result

            if (response.body!!.result !== RespondToSynchronizationPermittingChallengeResult.WRONG_CODE) {
                networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)
            }

            if (result === RespondToSynchronizationPermittingChallengeResult.ALLOWED) {
                remoteDevice.synchronizationPort = response.body!!.synchronizationPort
            }
        }
    }


    override fun requestStartSynchronization(remoteDevice: DiscoveredDevice, callback: SendRequestCallback<RequestStartSynchronizationResponseBody>) {
        networkSettings.addDevicesAskedForPermittingSynchronization(remoteDevice)

        val request = Request(CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME,
                RequestStartSynchronizationRequestBody(networkSettings.localHostDevice.uniqueDeviceId, networkSettings.synchronizationPort))

        requestSender.sendRequestAndReceiveResponseAsync(getSocketAddressFromDevice(remoteDevice), request,
                object : SendRequestCallback<RequestStartSynchronizationResponseBody> {
                    override fun done(response: Response<RequestStartSynchronizationResponseBody>) {
                        networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice)

                        callback.done(response)
                    }
                })
    }


    private fun getSocketAddressFromDevice(device: DiscoveredDevice): SocketAddress {
        return InetSocketAddress(device.address, device.messagesPort)
    }

}

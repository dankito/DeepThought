package net.dankito.data_access.network.communication.message

import net.dankito.data_access.network.communication.CommunicatorConfig
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.deepthought.model.INetworkSettings


class MessageHandler(protected var config: MessageHandlerConfig) : IMessageHandler {

    protected var networkSettings: INetworkSettings

    protected var challengeHandler: ChallengeHandler


    init {

        this.networkSettings = config.networkSettings

        this.challengeHandler = config.challengeHandler
    }


    override fun handleReceivedRequest(request: Request<*>, callback: (Response<out Any>) -> Unit) {
        when (request.method) {
            CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME -> callback(handleGetDeviceInfoRequest(request))
            CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME -> handleRequestPermitSynchronizationRequest(request as Request<DeviceInfo>, callback)
            CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME -> handleRespondToSynchronizationPermittingChallengeRequest(request as Request<RespondToSynchronizationPermittingChallengeRequestBody>, callback)
            CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME -> handleRequestStartSynchronizationRequest(request as Request<RequestStartSynchronizationRequestBody>, callback)
        }
    }


    private fun handleGetDeviceInfoRequest(request: Request<*>): Response<DeviceInfo> {
        return Response(DeviceInfo.fromDevice(networkSettings.localHostDevice))
    }


    private fun handleRequestPermitSynchronizationRequest(request: Request<DeviceInfo>, callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit) {
        request.body?.let { remoteDeviceInfo ->
            val permittingHandler = config.permissionHandler

            permittingHandler.shouldPermitSynchronizingWithDevice(remoteDeviceInfo) { _, permitsSynchronization ->
                handleShouldPermitSynchronizingWithDeviceResult(remoteDeviceInfo, permitsSynchronization, permittingHandler, callback)
            }
        }
    }

    private fun handleShouldPermitSynchronizingWithDeviceResult(remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean, permittingHandler: IsSynchronizationPermittedHandler,
                                                                callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit) {
        if (permitsSynchronization) {
            val (nonce, correctResponse) = challengeHandler.createChallengeForDevice(remoteDeviceInfo)
            permittingHandler.showCorrectResponseToUserNonBlocking(remoteDeviceInfo, correctResponse)

            callback(Response(RequestPermitSynchronizationResponseBody(
                    RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE, nonce)))
        }
        else {
            callback(Response(RequestPermitSynchronizationResponseBody(RequestPermitSynchronizationResult.DENIED)))
        }
    }

    private fun handleRespondToSynchronizationPermittingChallengeRequest(request: Request<RespondToSynchronizationPermittingChallengeRequestBody>,
                                                                         callback: (Response<RespondToSynchronizationPermittingChallengeResponseBody>) -> Unit) {
        request.body?.let { body ->
            var responseBody: RespondToSynchronizationPermittingChallengeResponseBody

            if (challengeHandler.isResponseOk(body.nonce, body.challengeResponse)) {
                addToPermittedSynchronizedDevices(body)

                responseBody = RespondToSynchronizationPermittingChallengeResponseBody(networkSettings.synchronizationPort)
            }
            else {
                responseBody = createWrongCodeResponse(body.nonce)
            }

            callback(Response(responseBody))
        }
    }

    protected fun addToPermittedSynchronizedDevices(requestBody: RespondToSynchronizationPermittingChallengeRequestBody) {
        challengeHandler.getDeviceInfoForNonce(requestBody.nonce)?.let { deviceInfo ->
            val deviceUniqueId = deviceInfo.uniqueDeviceId

            networkSettings.getDiscoveredDevice(deviceUniqueId)?.let { discoveredDevice ->
                discoveredDevice.synchronizationPort = requestBody.synchronizationPort

                networkSettings.addConnectedDevicePermittedToSynchronize(discoveredDevice)
            }
        }
    }

    private fun createWrongCodeResponse(nonce: String): RespondToSynchronizationPermittingChallengeResponseBody {
        val responseBody: RespondToSynchronizationPermittingChallengeResponseBody
        val countRetriesLeft = challengeHandler.getCountRetriesLeftForNonce(nonce)
        if (countRetriesLeft > 0) {
            responseBody = RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.WRONG_CODE, countRetriesLeft)
        } else {
            responseBody = RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.DENIED)
        }
        return responseBody
    }


    private fun handleRequestStartSynchronizationRequest(request: Request<RequestStartSynchronizationRequestBody>, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit) {
        request.body?.let { body ->
            if(isDevicePermittedToSynchronize(body.uniqueDeviceId) == false) {
                callback(Response(RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.DENIED)))
            }
            else {
                val permittedSynchronizedDevice = networkSettings.getDiscoveredDevice(body.uniqueDeviceId)
                if (permittedSynchronizedDevice != null) {
                    permittedSynchronizedDevice.synchronizationPort = body.synchronizationPort

                    networkSettings.addConnectedDevicePermittedToSynchronize(permittedSynchronizedDevice)
                }

                callback(Response(RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.ALLOWED, networkSettings.synchronizationPort)))
            }
        }
    }

    private fun isDevicePermittedToSynchronize(remoteDeviceUniqueId: String): Boolean {
        val synchronizedDevices = networkSettings.localHostDevice.synchronizedDevices

        for (synchronizedDevice in synchronizedDevices) {
            if (synchronizedDevice.uniqueDeviceId.equals(remoteDeviceUniqueId)) {
                return true
            }
        }

        return false
    }


    @Throws(Exception::class)
    override fun getRequestBodyClassForMethod(methodName: String): Class<*>? {
        when (methodName) {
            CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME -> return DeviceInfo::class.java
            CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME -> return RespondToSynchronizationPermittingChallengeRequestBody::class.java
            CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME -> return RequestStartSynchronizationRequestBody::class.java
            CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME -> return null // requests without request bodies
            else -> throw Exception("Don't know how to deserialize response of method " + methodName) // TODO: translate
        }
    }

    @Throws(Exception::class)
    override fun getResponseBodyClassForMethod(methodName: String): Class<*> {
        when (methodName) {
            CommunicatorConfig.GET_DEVICE_INFO_METHOD_NAME -> return DeviceInfo::class.java
            CommunicatorConfig.REQUEST_PERMIT_SYNCHRONIZATION_METHOD_NAME -> return RequestPermitSynchronizationResponseBody::class.java
            CommunicatorConfig.RESPONSE_TO_SYNCHRONIZATION_PERMITTING_CHALLENGE_METHOD_NAME -> return RespondToSynchronizationPermittingChallengeResponseBody::class.java
            CommunicatorConfig.REQUEST_START_SYNCHRONIZATION_METHOD_NAME -> return RequestStartSynchronizationResponseBody::class.java
            else -> throw Exception("Don't know how to deserialize response of method " + methodName) // TODO: translate
        }
    }

}

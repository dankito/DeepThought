package net.dankito.data_access.network.communication.message

import net.dankito.data_access.network.communication.CommunicatorConfig
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.INetworkSettings
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import kotlin.concurrent.thread


class MessageHandler(private var config: MessageHandlerConfig) : IMessageHandler {

    private val networkSettings: INetworkSettings = config.networkSettings

    private val challengeHandler: ChallengeHandler = config.challengeHandler

    private val registrationHandler: IDeviceRegistrationHandler = config.registrationHandler


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
            registrationHandler.shouldPermitSynchronizingWithDevice(remoteDeviceInfo) { _, permitsSynchronization ->
                thread { // callback mostly is executed on UI thread -> get off UI thread to avoid NetworkOnMainThreadException
                    handleShouldPermitSynchronizingWithDeviceResult(remoteDeviceInfo, permitsSynchronization, registrationHandler, callback)
                }
            }
        }
    }

    private fun handleShouldPermitSynchronizingWithDeviceResult(remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean, registrationHandler: IDeviceRegistrationHandler,
                                                                callback: (Response<RequestPermitSynchronizationResponseBody>) -> Unit) {
        if(permitsSynchronization) {
            val (nonce, correctResponse) = challengeHandler.createChallengeForDevice(remoteDeviceInfo)
            registrationHandler.showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo, correctResponse)

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
            val responseBody: RespondToSynchronizationPermittingChallengeResponseBody

            if (challengeHandler.isResponseOk(body.nonce, body.challengeResponse)) {
                val initialSyncDetails = addToPermittedSynchronizedDevices(body)

                if(initialSyncDetails != null) {
                    responseBody = RespondToSynchronizationPermittingChallengeResponseBody(networkSettings.synchronizationPort, initialSyncDetails)
                }
                else {
                    responseBody = RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.ERROR_OCCURRED)
                }
            }
            else {
                responseBody = createWrongCodeResponse(body.nonce)
            }

            callback(Response(responseBody))
        }
    }

    private fun addToPermittedSynchronizedDevices(body: RespondToSynchronizationPermittingChallengeRequestBody): SyncInfo? {
        getDiscoveredDevices(body)?.let { discoveredDevice ->
            discoveredDevice.synchronizationPort = body.synchronizationPort

            return registrationHandler.deviceHasBeenPermittedToSynchronize(discoveredDevice, body.syncInfo)
        }

        return null
    }

    private fun getDiscoveredDevices(body: RespondToSynchronizationPermittingChallengeRequestBody): DiscoveredDevice? {
        challengeHandler.getDeviceInfoForNonce(body.nonce)?.let { deviceInfo ->
            val deviceUniqueId = deviceInfo.uniqueDeviceId

            networkSettings.getDiscoveredDevice(deviceUniqueId)?.let { discoveredDevice ->
                return discoveredDevice
            }
        }

        return null
    }

    private fun createWrongCodeResponse(nonce: String): RespondToSynchronizationPermittingChallengeResponseBody {
        val responseBody: RespondToSynchronizationPermittingChallengeResponseBody
        val countRetriesLeft = challengeHandler.getCountRetriesLeftForNonce(nonce)

        if (countRetriesLeft > 0) {
            responseBody = RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.WRONG_CODE, countRetriesLeft)
        }
        else {
            responseBody = RespondToSynchronizationPermittingChallengeResponseBody(RespondToSynchronizationPermittingChallengeResult.DENIED)
        }

        return responseBody
    }


    private fun handleRequestStartSynchronizationRequest(request: Request<RequestStartSynchronizationRequestBody>, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit) {
        request.body?.let { body ->
            if(isDevicePermittedToSynchronize(body.deviceInfo.uniqueDeviceId) == false) {
                handleRemoteIsDeniedToSynchronize(body, callback)
            }
            else {
                handleRemoteIsPermittedToSynchronize(body, callback)
            }
        }
    }

    private fun handleRemoteIsDeniedToSynchronize(body: RequestStartSynchronizationRequestBody, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit) {
        val isRemoteDeviceKnown = config.entityManager.getAllEntitiesOfType(Device::class.java).filter { it.uniqueDeviceId == body.deviceInfo.uniqueDeviceId }.isNotEmpty()

        if(isRemoteDeviceKnown) {
            callback(Response(RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.DENIED)))
        }
        else { // mostly because remote synchronized with a device we also are synchronizing with, but there hasn't been a connection to this synchronized device till then
            callback(Response(RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.DO_NOT_KNOW_YOU)))
        }
    }

    private fun handleRemoteIsPermittedToSynchronize(body: RequestStartSynchronizationRequestBody, callback: (Response<RequestStartSynchronizationResponseBody>) -> Unit) {
        val permittedSynchronizedDevice = networkSettings.getDiscoveredDevice(body.deviceInfo.uniqueDeviceId)

        if(permittedSynchronizedDevice != null) {
            permittedSynchronizedDevice.synchronizationPort = body.synchronizationPort

            config.callRemoteRequestedToStartSynchronizationListeners(permittedSynchronizedDevice)
        }

        callback(Response(RequestStartSynchronizationResponseBody(RequestStartSynchronizationResult.ALLOWED, networkSettings.synchronizationPort)))
    }

    private fun isDevicePermittedToSynchronize(remoteDeviceUniqueId: String): Boolean {
        val synchronizedDevices = networkSettings.localUser.synchronizedDevices

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

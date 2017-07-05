package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.message.RequestPermitSynchronizationResult
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResponseBody
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResult
import net.dankito.data_access.network.communication.message.Response
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.service.synchronization.initialsync.model.DeepThoughtSyncInfo
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import net.dankito.service.synchronization.initialsync.model.UserSyncInfo
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService


abstract class DeviceRegistrationHandlerBase(protected val dataManager: DataManager, protected val initialSyncManager: InitialSyncManager, protected val dialogService: IDialogService,
                                             protected val localization: Localization) : IDeviceRegistrationHandler {

    private val requestingToSynchronizeWithRemoteListener = HashSet<(DiscoveredDevice) -> Unit>()

    private val newDeviceRegisteredListeners = HashSet<(DiscoveredDevice) -> Unit>()


    override fun showUnknownDeviceDiscovered(clientCommunicator: IClientCommunicator, unknownDevice: DiscoveredDevice) {
        showUnknownDeviceDiscoveredView(unknownDevice) { likesToRegister, neverAskAgainForThisDevice ->
            if(likesToRegister) {
                askForRegistration(clientCommunicator, unknownDevice)
            }
        }
    }

    abstract fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit)


    protected fun askForRegistration(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice) {
        clientCommunicator.requestPermitSynchronization(remoteDevice) { response ->
            response.body?.let { body ->
                if(body.result == RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
                    body.nonce?.let { getChallengeResponseFromUser(clientCommunicator, remoteDevice, it, false) }
                }
                else {
                    showAlertSynchronizingIsNotPermitted(remoteDevice)
                }
            }

            response.error?.let { error ->
                showErrorMessage(response)
            }
        }
    }

    protected fun getChallengeResponseFromUser(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice, nonce: String, wasCodePreviouslyWronglyEntered: Boolean) {
        var questionText = localization.getLocalizedString("alert.title.enter.response.code.for.permitting.synchronization", remoteDevice.device.getDisplayText())
        if (wasCodePreviouslyWronglyEntered) {
            questionText = localization.getLocalizedString("alert.title.entered.response.code.was.wrong", remoteDevice.device.getDisplayText())
        }

        dialogService.askForTextInput(questionText) { didEnterText, enteredResponse ->
            enteredResponse?.let { enteredResponse ->
                sendChallengeResponseToRemote(clientCommunicator, remoteDevice, nonce, enteredResponse);
            }
            if(didEnterText == false) {
//                networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice);
            }
        }
    }

    protected fun sendChallengeResponseToRemote(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice, nonce: String, enteredResponse: String) {
        callRequestingToSynchronizeWithRemoteListeners(remoteDevice) // opens synchronization port needed in respondToSynchronizationPermittingChallenge()

        clientCommunicator.respondToSynchronizationPermittingChallenge(remoteDevice, nonce, enteredResponse, createSyncInfo()) { response ->
            handleEnteredChallengeResponse(clientCommunicator, remoteDevice, response, nonce)
        }
    }

    private fun createSyncInfo(useCallerDatabaseIds: Boolean? = null, useCallerUserName: Boolean? = null): SyncInfo {
        val userSyncInfo = UserSyncInfo(dataManager.localUser)

        val deepThoughtSyncInfo = DeepThoughtSyncInfo(dataManager.deepThought)

        return SyncInfo(deepThoughtSyncInfo, userSyncInfo, useCallerDatabaseIds, useCallerUserName)
    }


    protected fun handleEnteredChallengeResponse(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice, response: Response<RespondToSynchronizationPermittingChallengeResponseBody>, nonce: String) {
        response.error?.let { showErrorMessage(response) }

        response.body?.let { body ->
            when(body.result) {
                RespondToSynchronizationPermittingChallengeResult.ALLOWED -> remoteAllowedSynchronization(remoteDevice, body)
                RespondToSynchronizationPermittingChallengeResult.WRONG_CODE -> getChallengeResponseFromUser(clientCommunicator, remoteDevice, nonce, true)
                RespondToSynchronizationPermittingChallengeResult.ERROR_OCCURRED -> showErrorMessage(response)
                else -> showAlertSynchronizingIsNotPermitted(remoteDevice)
            }
        }
    }

    protected fun remoteAllowedSynchronization(remoteDevice: DiscoveredDevice, body: RespondToSynchronizationPermittingChallengeResponseBody) {
        body.syncInfo?.let { syncInfo ->
            if(syncInfo.useCallerUserName ?: false) {
                initialSyncManager.syncUserInformationWithRemoteOnes(dataManager.localUser, syncInfo.user)
            }
            if(syncInfo.useCallerDatabaseIds ?: false) { // TODO: something is wrong here, see deviceHasBeenPermittedToSynchronize()
                initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(dataManager.deepThought, syncInfo)
            }
        }

        callNewDeviceRegisteredListeners(remoteDevice)
    }

    private fun showAlertSynchronizingIsNotPermitted(remoteDevice: DiscoveredDevice) {
        // TODO
    }

    private fun showErrorMessage(response: Response<out Any>) {
        // TODO
    }


    override fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo? {
        try {
            val localUser = dataManager.deepThought.localUser

            val useCallerDatabaseIds = ! initialSyncManager.shouldUseLocalDatabaseIds(dataManager.deepThought, remoteSyncInfo)
            val useCallerUserName = ! initialSyncManager.shouldUseLocalUserName(localUser, remoteSyncInfo.user)

            if(useCallerUserName) {
                initialSyncManager.syncUserInformationWithRemoteOnes(localUser, remoteSyncInfo.user)
            }
            if(useCallerDatabaseIds == false) { // TODO: something is wrong here, see remoteAllowedSynchronization()
                initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(dataManager.deepThought, remoteSyncInfo)
            }

            callNewDeviceRegisteredListeners(device)

            return createSyncInfo(useCallerDatabaseIds, useCallerUserName)
        } catch(e: Exception) {
            dialogService.showErrorMessage("Could not initialize synchronization with device ${device.device.getDisplayText()}", exception = e) // TODO: translate
        }

        return null
    }


    override fun addRequestingToSynchronizeWithRemoteListener(listener: (remoteDevice: DiscoveredDevice) -> Unit) {
        requestingToSynchronizeWithRemoteListener.add(listener)
    }

    private fun callRequestingToSynchronizeWithRemoteListeners(remoteDevice: DiscoveredDevice) {
        requestingToSynchronizeWithRemoteListener.forEach { it(remoteDevice) }
    }

    override fun addNewDeviceRegisteredListener(listener: (remoteDevice: DiscoveredDevice) -> Unit) {
        newDeviceRegisteredListeners.add(listener)
    }

    private fun callNewDeviceRegisteredListeners(remoteDevice: DiscoveredDevice) {
        newDeviceRegisteredListeners.forEach { it(remoteDevice) }
    }

}
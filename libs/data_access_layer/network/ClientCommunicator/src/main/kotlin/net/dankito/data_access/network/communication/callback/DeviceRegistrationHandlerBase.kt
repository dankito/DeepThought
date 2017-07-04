package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.message.RequestPermitSynchronizationResult
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResponseBody
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResult
import net.dankito.data_access.network.communication.message.Response
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.enums.ExtensibleEnumeration
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.service.synchronization.initialsync.model.DeepThoughtSyncInfo
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import net.dankito.service.synchronization.initialsync.model.UserSyncInfo
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService


abstract class DeviceRegistrationHandlerBase(protected val dataManager: DataManager, protected val initialSyncManager: InitialSyncManager, protected val dialogService: IDialogService,
                                             protected val localization: Localization) : IDeviceRegistrationHandler {


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
        clientCommunicator.respondToSynchronizationPermittingChallenge(remoteDevice, nonce, enteredResponse, createSyncInfo()) { response ->
                        handleEnteredChallengeResponse(clientCommunicator, remoteDevice, response, nonce)
        }
    }

    private fun createSyncInfo(useCallerDatabaseIds: Boolean? = null, useCallerUserName: Boolean? = null): SyncInfo {
        val user = dataManager.localUser
        val userSyncInfo = UserSyncInfo(user.id!!, user.userName, user.universallyUniqueId, user.firstName, user.lastName, user.synchronizedDevices.size, user.ignoredDevices.size)

        val deepThought = dataManager.deepThought
        val deepThoughtSyncInfo = DeepThoughtSyncInfo(deepThought.id!!, getMapForExtensibleEnumerationList(deepThought.applicationLanguages),
                getMapForExtensibleEnumerationList(deepThought.noteTypes), getMapForExtensibleEnumerationList(deepThought.noteTypes))

        return SyncInfo(deepThoughtSyncInfo, userSyncInfo, useCallerDatabaseIds, useCallerUserName)
    }

    private fun getMapForExtensibleEnumerationList(enumerationSet: Set<ExtensibleEnumeration>): Map<String, String> {
        return enumerationSet.filter { it.nameResourceKey != null }.associateBy( { it.nameResourceKey!! }, { it.id!! } )
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
            if(syncInfo.useCallerDatabaseIds ?: false) {
                initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(dataManager.deepThought, syncInfo)
            }
        }
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
            if(useCallerDatabaseIds == false) {
                initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(dataManager.deepThought, remoteSyncInfo)
            }

            return createSyncInfo(useCallerDatabaseIds, useCallerUserName)
        } catch(e: Exception) {
            dialogService.showErrorMessage("Could not initialize synchronization with device ${device.device.getDisplayText()}", exception = e) // TODO: translate
        }

        return null
    }
}
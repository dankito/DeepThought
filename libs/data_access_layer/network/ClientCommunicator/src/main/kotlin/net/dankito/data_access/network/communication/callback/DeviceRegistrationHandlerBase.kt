package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.message.RequestPermitSynchronizationResult
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResponseBody
import net.dankito.data_access.network.communication.message.RespondToSynchronizationPermittingChallengeResult
import net.dankito.data_access.network.communication.message.Response
import net.dankito.deepthought.model.ArticleSummaryExtractorConfig
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.service.synchronization.initialsync.model.DeepThoughtSyncInfo
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import net.dankito.service.synchronization.initialsync.model.UserSyncInfo
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.InputType
import org.slf4j.LoggerFactory


abstract class DeviceRegistrationHandlerBase(protected val dataManager: DataManager, private val initialSyncManager: InitialSyncManager, protected val dialogService: IDialogService,
                                             protected val localization: Localization) : IDeviceRegistrationHandler {

    companion object {
        private val log = LoggerFactory.getLogger(DeviceRegistrationHandlerBase::class.java)
    }


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


    private fun askForRegistration(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice) {
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
                showErrorMessage(error)
            }
        }
    }

    private fun getChallengeResponseFromUser(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice, nonce: String, wasCodePreviouslyWronglyEntered: Boolean) {
        var questionText = localization.getLocalizedString("alert.title.enter.response.code.for.permitting.synchronization", remoteDevice.device.getDisplayText())
        if(wasCodePreviouslyWronglyEntered) {
            questionText = localization.getLocalizedString("alert.title.entered.response.code.was.wrong", remoteDevice.device.getDisplayText())
        }

        dialogService.askForTextInput(questionText, type = InputType.Numbers) { didEnterText, enteredResponse ->
            enteredResponse?.let { enteredResponse ->
                sendChallengeResponseToRemote(clientCommunicator, remoteDevice, nonce, enteredResponse);
            }
            if(didEnterText == false) {
//                networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice);
            }
        }
    }

    private fun sendChallengeResponseToRemote(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice, nonce: String, enteredResponse: String) {
        callRequestingToSynchronizeWithRemoteListeners(remoteDevice) // opens synchronization port needed in respondToSynchronizationPermittingChallenge()

        clientCommunicator.respondToSynchronizationPermittingChallenge(remoteDevice, nonce, enteredResponse, createSyncInfo()) { response ->
            handleEnteredChallengeResponse(clientCommunicator, remoteDevice, response, nonce)
        }
    }

    private fun createSyncInfo(useCallerDatabaseIds: Boolean? = null, useCallerUserName: Boolean? = null): SyncInfo {
        val userSyncInfo = UserSyncInfo(dataManager.localUser)

        val deepThoughtSyncInfo = DeepThoughtSyncInfo(dataManager.deepThought)

        val articleSummaryExtractorConfigs = dataManager.entityManager.getAllEntitiesOfType(ArticleSummaryExtractorConfig::class.java)

        return SyncInfo(deepThoughtSyncInfo, userSyncInfo, articleSummaryExtractorConfigs, useCallerDatabaseIds, useCallerUserName)
    }


    private fun handleEnteredChallengeResponse(clientCommunicator: IClientCommunicator, remoteDevice: DiscoveredDevice, response: Response<RespondToSynchronizationPermittingChallengeResponseBody>, nonce: String) {
        response.error?.let { showErrorMessage(it) }

        response.body?.let { body ->
            when(body.result) {
                RespondToSynchronizationPermittingChallengeResult.ALLOWED -> remoteAllowedSynchronization(remoteDevice, body)
                RespondToSynchronizationPermittingChallengeResult.WRONG_CODE -> getChallengeResponseFromUser(clientCommunicator, remoteDevice, nonce, true)
                RespondToSynchronizationPermittingChallengeResult.ERROR_OCCURRED -> showErrorMessage()
                else -> showAlertSynchronizingIsNotPermitted(remoteDevice)
            }
        }
    }

    private fun remoteAllowedSynchronization(remoteDevice: DiscoveredDevice, body: RespondToSynchronizationPermittingChallengeResponseBody) {
        log.info("Remote allowed synchronization: $remoteDevice")

        body.syncInfo?.let { syncInfo ->
            // this is kind a dirty hack, newly synchronized device has to be added on both sides as otherwise it may gets overwritten. Don't know how to solve this otherwise
            initialSyncManager.syncUserDevices(dataManager.deepThought, syncInfo)

            if(syncInfo.useCallerDatabaseIds == false) {
                if(syncInfo.useCallerUserName == false) { // ensure that localUser object gets changed only on one side, otherwise there will be a conflict and chances are 50:50 which version will be used
                    initialSyncManager.syncUserInformationWithRemoteOnes(dataManager.localUser, syncInfo.user)
                }

                initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(dataManager.deepThought, syncInfo)
            }
        }

        callNewDeviceRegisteredListeners(remoteDevice)

        showDeviceRegistrationHasBeenSuccessfulMessage(remoteDevice)
    }

    private fun showDeviceRegistrationHasBeenSuccessfulMessage(remoteDevice: DiscoveredDevice) {
        val successMessage = localization.getLocalizedString("alert.message.registering.at.device.successful", remoteDevice.device.getDisplayText())

        dialogService.showInfoMessage(successMessage)
    }

    private fun showAlertSynchronizingIsNotPermitted(remoteDevice: DiscoveredDevice) {
        val remoteDidNotAllowSynchronization = localization.getLocalizedString("alert.message.remote.did.not.allow.synchronization", remoteDevice.device.getDisplayText())

        dialogService.showInfoMessage(remoteDidNotAllowSynchronization)
    }

    private fun showErrorMessage(error: Exception? = null) {
        dialogService.showErrorMessage(localization.getLocalizedString("alert.message.an.error.occurred"), exception = error)
    }


    override fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo? {
        log.info("Permitted device to synchronize: $device")

        try {
            val localUser = dataManager.deepThought.localUser

            val useCallerDatabaseIds = ! initialSyncManager.shouldUseLocalDatabaseIds(dataManager.deepThought, remoteSyncInfo)
            val useCallerUserName = ! initialSyncManager.shouldUseLocalUserName(localUser, remoteSyncInfo.user)

            // this is kind a dirty hack, newly synchronized device has to be added on both sides as otherwise it may gets overwritten. Don't know how to solve this otherwise
            initialSyncManager.syncUserDevices(dataManager.deepThought, remoteSyncInfo)

            if(useCallerDatabaseIds) {
                if(useCallerUserName) { // ensure that localUser object gets changed only on one side, otherwise there will be a conflict and chances are 50:50 which version will be used
                    initialSyncManager.syncUserInformationWithRemoteOnes(localUser, remoteSyncInfo.user)
                }

                initialSyncManager.syncLocalDatabaseIdsWithRemoteOnes(dataManager.deepThought, remoteSyncInfo)
            }

            callNewDeviceRegisteredListeners(device)

            return createSyncInfo(useCallerDatabaseIds, useCallerUserName)
        } catch(e: Exception) {
            log.error("Could do initial synchronization with device $device", e)
            dialogService.showErrorMessage(localization.getLocalizedString("alert.message.could.not.initialize.synchronization.with.device",
                    device.device.getDisplayText()), exception = e)
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
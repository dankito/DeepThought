package net.dankito.synchronization.device.messaging.callback

import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.synchronization.database.sync.InitialSyncManager
import net.dankito.synchronization.device.messaging.IMessenger
import net.dankito.synchronization.device.messaging.message.RequestPermitSynchronizationResult
import net.dankito.synchronization.device.messaging.message.RespondToSynchronizationPermittingChallengeResponseBody
import net.dankito.synchronization.device.messaging.message.RespondToSynchronizationPermittingChallengeResult
import net.dankito.synchronization.device.messaging.message.Response
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.synchronization.model.SyncInfo
import net.dankito.synchronization.model.UserSyncInfo
import net.dankito.util.localization.Localization
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.util.ui.dialog.InputType
import org.slf4j.LoggerFactory


abstract class DeviceRegistrationHandlerBase(protected val entityManager: IEntityManager, protected val networkSettings: NetworkSettings,
                                             protected val initialSyncManager: InitialSyncManager, protected val dialogService: IDialogService,
                                             protected val localization: Localization) : IDeviceRegistrationHandler {

    companion object {
        private val log = LoggerFactory.getLogger(DeviceRegistrationHandlerBase::class.java)
    }


    protected val requestingToSynchronizeWithRemoteListener = HashSet<(DiscoveredDevice) -> Unit>()

    protected val newDeviceRegisteredListeners = HashSet<(DiscoveredDevice) -> Unit>()

    protected val ignoreDeviceListeners = HashSet<(DiscoveredDevice) -> Unit>()


    override fun showUnknownDeviceDiscovered(messenger: IMessenger, unknownDevice: DiscoveredDevice) {
        showUnknownDeviceDiscoveredView(unknownDevice) { likesToRegister, neverAskAgainForThisDevice ->
            if(likesToRegister) {
                askForRegistration(messenger, unknownDevice)
            }
            if(neverAskAgainForThisDevice) {
                callIgnoreDeviceListeners(unknownDevice)
            }
        }
    }

    abstract fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit)


    protected open fun askForRegistration(messenger: IMessenger, remoteDevice: DiscoveredDevice) {
        messenger.requestPermitSynchronization(remoteDevice) { response ->
            response.body?.let { body ->
                if(body.result == RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
                    body.nonce?.let { getChallengeResponseFromUser(messenger, remoteDevice, it, false) }
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

    protected open fun getChallengeResponseFromUser(messenger: IMessenger, remoteDevice: DiscoveredDevice, nonce: String, wasCodePreviouslyWronglyEntered: Boolean) {
        var questionText = localization.getLocalizedString("alert.title.enter.response.code.for.permitting.synchronization", remoteDevice.device.getDisplayText())
        if(wasCodePreviouslyWronglyEntered) {
            questionText = localization.getLocalizedString("alert.title.entered.response.code.was.wrong", remoteDevice.device.getDisplayText())
        }

        dialogService.askForTextInput(questionText, type = InputType.Numbers) { didEnterText, enteredResponse ->
            enteredResponse?.let { enteredResponse ->
                sendChallengeResponseToRemote(messenger, remoteDevice, nonce, enteredResponse);
            }
            if(didEnterText == false) {
//                networkSettings.removeDevicesAskedForPermittingSynchronization(remoteDevice);
            }
        }
    }

    protected open fun sendChallengeResponseToRemote(messenger: IMessenger, remoteDevice: DiscoveredDevice, nonce: String, enteredResponse: String) {
        callRequestingToSynchronizeWithRemoteListeners(remoteDevice) // opens synchronization port needed in respondToSynchronizationPermittingChallenge()

        messenger.respondToSynchronizationPermittingChallenge(remoteDevice, nonce, enteredResponse, createSyncInfo()) { response ->
            handleEnteredChallengeResponse(messenger, remoteDevice, response, nonce)
        }
    }

    protected open fun createSyncInfo(useCallerDatabaseIds: Boolean? = null, useCallerUserName: Boolean? = null): SyncInfo {
        val userSyncInfo = UserSyncInfo(networkSettings.localUser)

        return SyncInfo(networkSettings.localHostDevice.id!!, userSyncInfo, useCallerDatabaseIds, useCallerUserName)
    }


    private fun handleEnteredChallengeResponse(messenger: IMessenger, remoteDevice: DiscoveredDevice, response: Response<RespondToSynchronizationPermittingChallengeResponseBody>, nonce: String) {
        response.error?.let { showErrorMessage(it) }

        response.body?.let { body ->
            when(body.result) {
                RespondToSynchronizationPermittingChallengeResult.ALLOWED -> remoteAllowedSynchronization(remoteDevice, body)
                RespondToSynchronizationPermittingChallengeResult.WRONG_CODE -> getChallengeResponseFromUser(messenger, remoteDevice, nonce, true)
                RespondToSynchronizationPermittingChallengeResult.ERROR_OCCURRED -> showErrorMessage()
                else -> showAlertSynchronizingIsNotPermitted(remoteDevice)
            }
        }
    }

    protected open fun remoteAllowedSynchronization(remoteDevice: DiscoveredDevice, body: RespondToSynchronizationPermittingChallengeResponseBody) {
        log.info("Remote allowed synchronization: $remoteDevice")

        body.syncInfo?.let { syncInfo ->
            doInitialSynchronization(syncInfo, syncInfo.useCallerDatabaseIds == false, syncInfo.useCallerUserName == false)
        }

        callNewDeviceRegisteredListeners(remoteDevice)

        showDeviceRegistrationHasBeenSuccessfulMessage(remoteDevice)
    }

    protected open fun showDeviceRegistrationHasBeenSuccessfulMessage(remoteDevice: DiscoveredDevice) {
        val successMessage = localization.getLocalizedString("alert.message.registering.at.device.successful", remoteDevice.device.getDisplayText())

        dialogService.showInfoMessage(successMessage)
    }

    protected open fun showAlertSynchronizingIsNotPermitted(remoteDevice: DiscoveredDevice) {
        val remoteDidNotAllowSynchronization = localization.getLocalizedString("alert.message.remote.did.not.allow.synchronization", remoteDevice.device.getDisplayText())

        dialogService.showInfoMessage(remoteDidNotAllowSynchronization)
    }

    protected open fun showErrorMessage(error: Exception? = null) {
        dialogService.showErrorMessage(localization.getLocalizedString("alert.message.an.error.occurred"), exception = error)
    }


    override fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo? {
        log.info("Permitted device to synchronize: $device")

        try {
            val localUser = networkSettings.localUser

            val useCallerDatabaseIds = ! initialSyncManager.shouldUseLocalDatabaseIds(networkSettings.localUser, remoteSyncInfo)
            val useCallerUserName = ! initialSyncManager.shouldUseLocalUserName(localUser, remoteSyncInfo.user)

            doInitialSynchronization(remoteSyncInfo, useCallerDatabaseIds, useCallerUserName)

            callNewDeviceRegisteredListeners(device)

            return createSyncInfo(useCallerDatabaseIds, useCallerUserName)
        } catch(e: Exception) {
            log.error("Could do initial synchronization with device $device", e)
            dialogService.showErrorMessage(localization.getLocalizedString("alert.message.could.not.initialize.synchronization.with.device",
                    device.device.getDisplayText()), exception = e)
        }

        return null
    }

    protected open fun doInitialSynchronization(syncInfo: SyncInfo, syncDatabaseIds: Boolean, syncUserInfo: Boolean) {
        // this is kind a dirty hack, newly synchronized device has to be added on both sides as otherwise it may gets overwritten. Don't know how to solve this otherwise
        initialSyncManager.syncUserDevices(networkSettings.localHostDevice.id!!, networkSettings.localUser, syncInfo)

        if(syncDatabaseIds) {
            if(syncUserInfo) { // ensure that localUser object gets changed only on one side, otherwise there will be a conflict and chances are 50:50 which version will be used
                initialSyncManager.syncUserInformationWithRemoteOnes(networkSettings.localUser, syncInfo.user)
            }
        }
    }


    override fun addRequestingToSynchronizeWithRemoteListener(listener: (remoteDevice: DiscoveredDevice) -> Unit) {
        requestingToSynchronizeWithRemoteListener.add(listener)
    }

    protected open fun callRequestingToSynchronizeWithRemoteListeners(remoteDevice: DiscoveredDevice) {
        requestingToSynchronizeWithRemoteListener.forEach { it(remoteDevice) }
    }

    override fun addNewDeviceRegisteredListener(listener: (remoteDevice: DiscoveredDevice) -> Unit) {
        newDeviceRegisteredListeners.add(listener)
    }

    protected open fun callNewDeviceRegisteredListeners(remoteDevice: DiscoveredDevice) {
        newDeviceRegisteredListeners.forEach { it(remoteDevice) }
    }

    override fun addIgnoreDeviceListener(listener: (remoteDevice: DiscoveredDevice) -> Unit) {
        ignoreDeviceListeners.add(listener)
    }

    protected open fun callIgnoreDeviceListeners(remoteDevice: DiscoveredDevice) {
        ignoreDeviceListeners.forEach { it(remoteDevice) }
    }

}
package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.IClientCommunicator
import net.dankito.data_access.network.communication.message.RequestPermitSynchronizationResponseBody
import net.dankito.data_access.network.communication.message.RequestPermitSynchronizationResult
import net.dankito.data_access.network.communication.message.Response
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService


abstract class DeviceRegistrationHandlerBase(protected val dialogService: IDialogService, protected val localization: Localization) : IDeviceRegistrationHandler {


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
        clientCommunicator.respondToSynchronizationPermittingChallenge(remoteDevice, nonce, enteredResponse) { response ->
            //            handleEnteredChallengeResponse(remoteDevice, response, nonce)
        }
    }

    private fun showAlertSynchronizingIsNotPermitted(remoteDevice: DiscoveredDevice) {
        // TODO
    }

    private fun showErrorMessage(response: Response<RequestPermitSynchronizationResponseBody>) {
        // TODO
    }
}
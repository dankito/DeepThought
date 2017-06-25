package net.dankito.deepthought.javafx.service.communication

import javafx.application.Platform
import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.data_access.network.communication.callback.ShouldPermitSynchronizingWithDeviceCallback
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.javafx.util.FXUtils


class JavaFXIsSynchronizationPermittedHandler : IsSynchronizationPermittedHandler {

    //  @Inject
    //  protected Localization localization;


    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: ShouldPermitSynchronizingWithDeviceCallback) {
        FXUtils.runOnUiThread { shouldPermitSynchronizingWithDeviceOnUiThread(remoteDeviceInfo, callback) }
    }

    protected fun shouldPermitSynchronizingWithDeviceOnUiThread(remoteDeviceInfo: DeviceInfo, callback: ShouldPermitSynchronizingWithDeviceCallback) {
        // TODO
        //    String message = localization.getLocalizedString("alert.message.permit.device.to.synchronize", remoteDeviceInfo);
        //    String alertTitle = localization.getLocalizedString("alert.title.permit.device.to.synchronize");
        //
        //    boolean permitsSynchronization = AlertHelper.showConfirmationDialogOnUiThread(message, alertTitle);

        val permitsSynchronization = true

        callback.done(remoteDeviceInfo, permitsSynchronization)
    }

    override fun showCorrectResponseToUserNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        Platform.runLater { showCorrectResponseToUserOnUiThread(remoteDeviceInfo, correctResponse) }
    }

    fun showCorrectResponseToUserOnUiThread(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        // TODO
        //    String message = localization.getLocalizedString("alert.message.enter.this.code.on.remote.device", remoteDeviceInfo, correctResponse);
        //
        //    AlertHelper.showInfoMessage(message, null);
    }

}

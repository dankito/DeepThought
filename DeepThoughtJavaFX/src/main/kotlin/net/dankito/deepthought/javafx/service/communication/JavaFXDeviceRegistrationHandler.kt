package net.dankito.deepthought.javafx.service.communication

import javafx.application.Platform
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.DeepThought
import net.dankito.synchronization.database.IEntityManager
import net.dankito.synchronization.database.sync.DeepThoughtInitialSyncManager
import net.dankito.synchronization.device.messaging.callback.DeepThoughtDeviceRegistrationHandlerBase
import net.dankito.synchronization.device.messaging.message.DeviceInfo
import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.NetworkSettings
import net.dankito.util.localization.Localization
import net.dankito.util.ui.dialog.ConfirmationDialogButton
import net.dankito.util.ui.dialog.IDialogService
import tornadofx.*


class JavaFXDeviceRegistrationHandler(deepThought: DeepThought, entityManager: IEntityManager, networkSettings: NetworkSettings, initialSyncManager: DeepThoughtInitialSyncManager, dialogService: IDialogService, localization: Localization)
    : DeepThoughtDeviceRegistrationHandlerBase(deepThought, entityManager, networkSettings, initialSyncManager, dialogService, localization) {

    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
        val message = localization.getLocalizedString("alert.message.permit.device.to.synchronize", remoteDeviceInfo);
        val alertTitle = localization.getLocalizedString("alert.title.permit.device.to.synchronize");

        dialogService.showConfirmationDialog(message, alertTitle) { selectedButton ->
            callback(remoteDeviceInfo, selectedButton == ConfirmationDialogButton.Confirm)
        }
    }

    override fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        val message = localization.getLocalizedString("alert.message.enter.this.code.on.remote.device", remoteDeviceInfo, correctResponse);

        Platform.runLater { // so that it's really non blocking
            dialogService.showInfoMessage(message, null)
        }
    }


    override fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        FXUtils.runOnUiThread {
            FX.primaryStage.show()
            FX.primaryStage.requestFocus()
            FX.primaryStage.toFront()
        }

        var message = localization.getLocalizedString("alert.message.ask.synchronize.data.with.device")
        message += localization.getLocalizedString("device.info", unknownDevice.device.getDisplayText())

        message += localization.getLocalizedString("ip.address", unknownDevice.address)

        dialogService.showConfirmationDialog(message, localization.getLocalizedString("alert.title.ask.synchronize.data.with.device")) { selectedButton ->
            if(selectedButton == ConfirmationDialogButton.Confirm) {
                callback(true, false)
            }
        }
    }

    override fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
        // TODO: with current implementation there's no way to get instance of AlertDialog so that we cannot dismiss it
    }

}

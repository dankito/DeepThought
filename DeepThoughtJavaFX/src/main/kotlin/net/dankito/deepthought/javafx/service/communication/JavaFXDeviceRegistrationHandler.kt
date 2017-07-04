package net.dankito.deepthought.javafx.service.communication

import javafx.application.Platform
import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import tornadofx.*


class JavaFXDeviceRegistrationHandler(dataManager: DataManager, initialSyncManager: InitialSyncManager, dialogService: IDialogService, localization: Localization)
    : DeviceRegistrationHandlerBase(dataManager, initialSyncManager, dialogService, localization) {

    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
        val message = localization.getLocalizedString("alert.message.permit.device.to.synchronize", remoteDeviceInfo);
        val alertTitle = localization.getLocalizedString("alert.title.permit.device.to.synchronize");

        dialogService.showConfirmationDialog(message, alertTitle) { permitsSynchronization ->
            callback(remoteDeviceInfo, permitsSynchronization)
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

        dialogService.showConfirmationDialog(message, localization.getLocalizedString("alert.title.ask.synchronize.data.with.device")) { likesToSyncData ->
            if(likesToSyncData) {
                callback(true, false)
            }
        }
    }

    override fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
        // TODO: with current implementation there's no way to get instance of AlertDialog so that we cannot dismiss it
    }

}

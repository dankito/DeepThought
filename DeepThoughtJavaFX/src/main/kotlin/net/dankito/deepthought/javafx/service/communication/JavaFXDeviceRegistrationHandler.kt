package net.dankito.deepthought.javafx.service.communication

import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import tornadofx.*


class JavaFXDeviceRegistrationHandler(private val dialogService: IDialogService, private val localization: Localization)
    : IDeviceRegistrationHandler {


    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
        val message = localization.getLocalizedString("alert.message.permit.device.to.synchronize", remoteDeviceInfo);
        val alertTitle = localization.getLocalizedString("alert.title.permit.device.to.synchronize");

        dialogService.showConfirmationDialog(message, alertTitle) { permitsSynchronization ->
            callback(remoteDeviceInfo, permitsSynchronization)
        }
    }

    override fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        val message = localization.getLocalizedString("alert.message.enter.this.code.on.remote.device", remoteDeviceInfo, correctResponse);

        dialogService.showInfoMessage(message, null)
    }

}

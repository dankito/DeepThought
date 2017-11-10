package net.dankito.deepthought.android.service.communication

import android.content.Context
import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.SnackbarService
import net.dankito.deepthought.android.service.StringUtil
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.service.synchronization.initialsync.model.SyncInfo
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService


class AndroidDeviceRegistrationHandler(private var context: Context, dataManager: DataManager, initialSyncManager: InitialSyncManager, dialogService: IDialogService,
                                       localization: Localization, private var snackbarService: SnackbarService)
    : DeviceRegistrationHandlerBase(dataManager, initialSyncManager, dialogService, localization) {


    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
        val message = context.getString(R.string.alert_message_permit_device_to_synchronize, remoteDeviceInfo)
        val alertTitle = context.getString(R.string.alert_title_permit_device_to_synchronize)

        dialogService.showConfirmationDialog(message, alertTitle) { permitsSynchronization ->
            callback(remoteDeviceInfo, permitsSynchronization)
        }
    }


    override fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        val htmlFormattedMessage = context.getString(R.string.alert_message_enter_this_code_on_remote_device, remoteDeviceInfo.toString(), correctResponse)
        val message = StringUtil().getSpannedFromHtml(htmlFormattedMessage)

        dialogService.showInfoMessage(message)
    }


    override fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
        snackbarService.checkIfSnackbarForDeviceShouldBeDismissed(disconnectedDevice)
    }

    override fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        snackbarService.showUnknownDeviceDiscoveredView(unknownDevice, callback)
    }


    override fun deviceHasBeenPermittedToSynchronize(device: DiscoveredDevice, remoteSyncInfo: SyncInfo): SyncInfo? {
        val result = super.deviceHasBeenPermittedToSynchronize(device, remoteSyncInfo)

        snackbarService.checkIfSnackbarForDeviceShouldBeDismissed(device)

        return result
    }

}

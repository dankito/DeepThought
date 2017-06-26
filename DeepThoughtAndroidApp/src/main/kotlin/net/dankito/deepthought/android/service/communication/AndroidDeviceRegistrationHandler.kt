package net.dankito.deepthought.android.service.communication

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService




class AndroidDeviceRegistrationHandler(private var context: Context, private val dialogService: IDialogService,
                                       private val localization: Localization, private var currentActivityTracker: CurrentActivityTracker)
    : IDeviceRegistrationHandler {

    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
        val message = context.getString(R.string.alert_message_permit_device_to_synchronize, remoteDeviceInfo)
        val alertTitle = context.getString(R.string.alert_title_permit_device_to_synchronize)

        dialogService.showConfirmationDialog(message, alertTitle) { permitsSynchronization ->
            callback(remoteDeviceInfo, permitsSynchronization)
        }
    }


    override fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        val message = context.getString(R.string.alert_message_enter_this_code_on_remote_device, remoteDeviceInfo.toString(), correctResponse)

        dialogService.showInfoMessage(message)
    }

}

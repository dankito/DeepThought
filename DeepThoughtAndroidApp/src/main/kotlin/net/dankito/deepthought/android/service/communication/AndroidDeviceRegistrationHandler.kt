package net.dankito.deepthought.android.service.communication

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.snackbar_ask_sync_data_with_device.view.*
import net.dankito.data_access.network.communication.callback.DeviceRegistrationHandlerBase
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.synchronization.initialsync.InitialSyncManager
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import java.util.concurrent.ConcurrentHashMap


class AndroidDeviceRegistrationHandler(private var context: Context, dataManager: DataManager, initialSyncManager: InitialSyncManager, dialogService: IDialogService,
                                       localization: Localization, private var currentActivityTracker: CurrentActivityTracker)
    : DeviceRegistrationHandlerBase(dataManager, initialSyncManager, dialogService, localization) {

    private var snackbarAskToSyncDataWithDevice: Snackbar? = null

    private var deviceIdShowingSnackbarFor: String? = null

    private val unknownDevicesWaitingToShowNotificationToUser = ConcurrentHashMap<DiscoveredDevice, (Boolean, Boolean) -> Unit>()


    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: (remoteDeviceInfo: DeviceInfo, permitsSynchronization: Boolean) -> Unit) {
        val message = context.getString(R.string.alert_message_permit_device_to_synchronize, remoteDeviceInfo)
        val alertTitle = context.getString(R.string.alert_title_permit_device_to_synchronize)

        dialogService.showConfirmationDialog(message, alertTitle) { permitsSynchronization ->
            callback(remoteDeviceInfo, permitsSynchronization)
        }
    }


    override fun showResponseToEnterOnOtherDeviceNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        val htmlFormattedMessage = context.getString(R.string.alert_message_enter_this_code_on_remote_device, remoteDeviceInfo.toString(), correctResponse)
        val message = Html.fromHtml(htmlFormattedMessage)

        dialogService.showInfoMessage(message)
    }


    override fun unknownDeviceDisconnected(disconnectedDevice: DiscoveredDevice) {
        if(deviceIdShowingSnackbarFor == disconnectedDevice.device.id) {
            currentActivityTracker.currentActivity?.let { activity ->
                snackbarAskToSyncDataWithDevice?.dismiss()
            }
        }
    }

    override fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        if(snackbarAskToSyncDataWithDevice != null) { // Snackbar for another device already shown
            unknownDevicesWaitingToShowNotificationToUser.put(unknownDevice, callback)
            return
        }

        currentActivityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { askUserToSyncDataWithDeviceOnMainThread(activity, unknownDevice, callback) }
        }
    }

    private fun askUserToSyncDataWithDeviceOnMainThread(currentActivity: Activity, remoteDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        val rootView = currentActivity.findViewById(android.R.id.content)

        synchronized(this) {
            snackbarAskToSyncDataWithDevice = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE)
            deviceIdShowingSnackbarFor = remoteDevice.device.id
        }

        snackbarAskToSyncDataWithDevice?.let { snackbar ->
            snackbar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    snackbarDismmissed(remoteDevice)
                }
            })

            snackbar.setAction(android.R.string.ok) { callback(true, false) }

            // Warning: Actually a Snackbar's Design should not be manipulated:
            // "Don't customize the Snackbar. It should not contain any more elements than a short text and one action. See Google Material design guidelines."
            // Code found at: http://stackoverflow.com/questions/32453946/how-to-customize-snackbars-layout
            customizeAskToSyncDataWithDeviceSnackbar(snackbar, currentActivity, remoteDevice, rootView.getWidth())

            snackbar.show()
        }
    }

    private fun customizeAskToSyncDataWithDeviceSnackbar(snackbar: Snackbar, activity: Activity, device: DiscoveredDevice, windowWidth: Int) {
        val layout = snackbar.view as Snackbar.SnackbarLayout
        centerActionTextViewVertically(layout)

        val txtvwSnackbarStandardText = layout.findViewById(android.support.design.R.id.snackbar_text) as TextView
        txtvwSnackbarStandardText.visibility = View.INVISIBLE

        // Inflate our custom view
        val snackView = activity.layoutInflater.inflate(R.layout.snackbar_ask_sync_data_with_device, null)

        // Configure the view
        snackView.imgvwDeviceIcon.setImageResource(getOsLogoId(device.device))

        snackView.txtvwDeviceInfo.text = device.device.getDisplayText()
        snackView.txtvwDeviceInfo.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        snackView.txtvwDeviceAddress.text = device.address + "?" // TODO: not that good, create a string resource and include question mark (e.g. Spanish uses two)
        snackView.txtvwDeviceAddress.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        snackView.txtvwAskSynchronizeDataWithThisDevice.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        snackView.chkbxNeverAskAgainToConnectWithThisDevice.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
    }

    private fun centerActionTextViewVertically(layout: Snackbar.SnackbarLayout) {
        layout.getChildAt(0)?.let { snackbarContentLayout ->
            val layoutParams = snackbarContentLayout.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            snackbarContentLayout.layoutParams = layoutParams
        }
    }

    private fun getOsLogoId(device: Device): Int {
        val osName = device.osName.toLowerCase()

        if(osName.contains("android")) {
            return R.drawable.ic_android
        }
        else if(osName.contains("linux")) {
            return R.drawable.ic_linux
        }
        else if(osName.contains("windows")) {
            return R.drawable.ic_windows
        }
        else if(osName.contains("mac")) {
            return R.drawable.ic_apple // TODO: this black / dark gray is not that well visible on Snackbar's dark background
        }
        else if(osName.contains("solaris")) {
//            return R.drawable.sun_solaris_logo // TODO
        }

        return 0 // TODO: create a placeholder logo
    }

    private fun snackbarDismmissed(remoteDevice: DiscoveredDevice) {
        synchronized(this) {
            if(deviceIdShowingSnackbarFor == remoteDevice.device.id) { // otherwise when a second snackbar gets display, shortly after for first one dismiss is called and that call resets snackbarAskToSyncDataWithDevice and deviceIdShowingSnackbarFor
                snackbarAskToSyncDataWithDevice = null
                deviceIdShowingSnackbarFor = null

                unknownDevicesWaitingToShowNotificationToUser.entries.firstOrNull()?.let {
                    unknownDevicesWaitingToShowNotificationToUser.remove(it.key)
                    showUnknownDeviceDiscoveredView(it.key, it.value)
                }
            }
        }
    }

}

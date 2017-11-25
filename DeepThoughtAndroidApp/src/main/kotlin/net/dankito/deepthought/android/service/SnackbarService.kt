package net.dankito.deepthought.android.service

import android.app.Activity
import android.net.Uri
import android.support.design.internal.SnackbarContentLayout
import android.support.design.widget.Snackbar
import android.text.Spanned
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.snackbar_ask_sync_data_with_device.view.*
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.di.AppComponent
import net.dankito.deepthought.android.extensions.getColorFromResourceId
import net.dankito.deepthought.android.extensions.setTintColor
import net.dankito.deepthought.model.Device
import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.ui.IRouter
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.concurrent.schedule


class SnackbarService {

    companion object {
        const val PeriodToWaitBeforeShowingFirstSnackbarOnStartUp = 2000L

        private const val PeriodToWaitBeforeShowingNextSnackbar = 2000L

        private val log = LoggerFactory.getLogger(SnackbarService::class.java)
    }


    @Inject
    protected lateinit var currentActivityTracker: CurrentActivityTracker

    @Inject
    protected lateinit var router: IRouter


    private var currentSnackbar: Snackbar? = null

    private var currentSnackbarId: Any? = null

    private val unknownDevicesWaitingToShowNotificationToUser = ConcurrentHashMap<DiscoveredDevice, (Boolean, Boolean) -> Unit>()

    private val stringUtil = StringUtil()


    init {
        AppComponent.component.inject(this)
    }


    fun showUrlInClipboardDetectedSnackbar(activity: Activity, url: String, actionInvokedListener: () -> Unit) {
        try {
            val text = showUrlInClipboardDetectedSnackbarText(url, activity)
            showSnackbar(text, url, activity, { customizeUrlInClipboardDetectedSnackbar(activity, it) }) {
                actionInvokedListener()
            }
        } catch(e: Exception) { log.error("Could not show snackbar for Clipboard url $url", e) }
    }

    private fun showUrlInClipboardDetectedSnackbarText(url: String, activity: Activity): Spanned {
        val uri = Uri.parse(url)
        var host = uri.host
        if (host.startsWith("www.")) {
            host = host.substring(4)
        }

        val unformattedText = activity.getText(R.string.snackbar_extract_item_from_url).toString().replace("%1\$s", host)

        return stringUtil.getSpannedFromHtml(unformattedText)
    }

    private fun customizeUrlInClipboardDetectedSnackbar(activity: Activity, snackbar: Snackbar) {
        val textColor = activity.getColorFromResourceId(R.color.colorPrimary)
        snackbar.setActionTextColor(textColor)

        (snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as? TextView)?.let { txtvwSnackbarStandardText ->
            txtvwSnackbarStandardText.setTextColor(textColor)
            txtvwSnackbarStandardText.maxLines = 4
        }

        val icon = ImageView(activity)
        icon.setImageResource(R.drawable.ic_help_outline_white_48dp)
        icon.setTintColor(R.color.colorPrimary)

        addIconToSnackbar(snackbar, icon)
    }


    private fun showUnknownDeviceDiscoveredViewAfterDelay(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        Timer().schedule(PeriodToWaitBeforeShowingNextSnackbar) {
            showUnknownDeviceDiscoveredView(unknownDevice, callback)
        }
    }

    fun showUnknownDeviceDiscoveredView(unknownDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        if(currentSnackbar != null) { // another Snackbar already shown
            unknownDevicesWaitingToShowNotificationToUser.put(unknownDevice, callback)
            return
        }

        val activity = currentActivityTracker.currentActivity
        if(activity != null) {
            activity.runOnUiThread { askUserToSyncDataWithDeviceOnMainThread(activity, unknownDevice, callback) }
        }
        else { // when there's not current activity, e.g. only DeepThoughtBackgroundAndroidService is running but no UI is displayed, wait till UI is available again
            currentActivityTracker.addNextActivitySetListener {
                showUnknownDeviceDiscoveredViewAfterDelay(unknownDevice, callback)
            }
        }
    }

    private fun askUserToSyncDataWithDeviceOnMainThread(currentActivity: Activity, remoteDevice: DiscoveredDevice, callback: (Boolean, Boolean) -> Unit) {
        showSnackbar("", remoteDevice.device.id ?: "", currentActivity, { customizeAskToSyncDataWithDeviceSnackbar(it, currentActivity, remoteDevice) }) {
            val neverAskAgain = it.view.chkbxNeverAskAgainToConnectWithThisDevice.isChecked
            callback(!neverAskAgain, neverAskAgain)
        }
    }

    private fun customizeAskToSyncDataWithDeviceSnackbar(snackbar: Snackbar, activity: Activity, device: DiscoveredDevice) {
        val layout = snackbar.view as Snackbar.SnackbarLayout

        val txtvwSnackbarStandardText = layout.findViewById(android.support.design.R.id.snackbar_text) as TextView
        txtvwSnackbarStandardText.visibility = View.INVISIBLE

        val snackView = activity.layoutInflater.inflate(R.layout.snackbar_ask_sync_data_with_device, null)

        snackView.imgvwDeviceIcon.setImageResource(getOsLogoId(device.device))

        snackView.txtvwDeviceInfo.text = device.device.getDisplayText()
        snackView.txtvwDeviceInfo.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        snackView.txtvwDeviceAddress.text = device.address + "?" // TODO: not that good, create a string resource and include question mark (e.g. Spanish uses two)
        snackView.txtvwDeviceAddress.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        snackView.txtvwAskSynchronizeDataWithThisDevice.setTextColor(txtvwSnackbarStandardText.currentTextColor)

        snackView.chkbxNeverAskAgainToConnectWithThisDevice.setTextColor(txtvwSnackbarStandardText.currentTextColor)
        snackView.chkbxNeverAskAgainToConnectWithThisDevice.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                (layout.findViewById(android.support.design.R.id.snackbar_action) as Button).setText(R.string.action_never)
            }
            else {
                (layout.findViewById(android.support.design.R.id.snackbar_action) as Button).setText(android.R.string.ok)
            }
        }

        layout.addView(snackView, 0)
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


    fun checkIfSnackbarForDeviceShouldBeDismissed(device: DiscoveredDevice) {
        if(currentSnackbarId == device.device.id) {
            currentActivityTracker.currentActivity?.let { activity ->
                activity.runOnUiThread { currentSnackbar?.dismiss() }
            }
        }

        unknownDevicesWaitingToShowNotificationToUser.remove(device)
    }


    private fun showSnackbar(text: CharSequence, snackbarId: Any, activity: Activity, customizeSnackbarListener: ((Snackbar) -> Unit)? = null, actionInvokedListener: (Snackbar) -> Unit) {
        var rootView = activity.findViewById(R.id.content_layout_root) // content_layout_root only works for MainActivity -> find a generic solution
        if(rootView == null) {
            rootView = activity.findViewById(android.R.id.content)
        }

        synchronized(this) {
            currentSnackbar = Snackbar.make(rootView, text, Snackbar.LENGTH_INDEFINITE)
            currentSnackbarId = snackbarId
        }

        currentSnackbar?.let { snackbar ->
            snackbar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    snackbarDismmissed(snackbarId)
                }
            })

            val layout = snackbar.view as Snackbar.SnackbarLayout
            centerActionTextViewVertically(layout)

            snackbar.setAction(android.R.string.ok) { actionInvokedListener(snackbar) }

            // Warning: Actually a Snackbar's Design should not be manipulated:
            // "Don't customize the Snackbar. It should not contain any more elements than a short text and one action. See Google Material design guidelines."
            // Code found at: http://stackoverflow.com/questions/32453946/how-to-customize-snackbars-layout
            customizeSnackbarListener?.invoke(snackbar)

            snackbar.show()
        }
    }

    private fun snackbarDismmissed(snackbarId: Any) {
        synchronized(this) {
            if(snackbarId == currentSnackbarId) { // otherwise when a second snackbar gets display, shortly after for first one dismiss is called and that call resets snackbarAskToSyncDataWithDevice and deviceIdShowingSnackbarFor
                currentSnackbar = null
                currentSnackbarId = null

                unknownDevicesWaitingToShowNotificationToUser.entries.firstOrNull()?.let {
                    unknownDevicesWaitingToShowNotificationToUser.remove(it.key)
                    showUnknownDeviceDiscoveredViewAfterDelay(it.key, it.value)
                }
            }
        }
    }

    private fun centerActionTextViewVertically(layout: Snackbar.SnackbarLayout) {
        layout.getChildAt(0)?.let { snackbarContentLayout ->
            val layoutParams = snackbarContentLayout.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            snackbarContentLayout.layoutParams = layoutParams
        }
    }

    private fun addIconToSnackbar(snackbar: Snackbar, icon: ImageView) {
        (snackbar.view as? Snackbar.SnackbarLayout)?.let { snackbarLayout ->
            for (i in 0..snackbarLayout.childCount - 1) {
                val child = snackbarLayout.getChildAt(i)
                if (child is SnackbarContentLayout) {
                    child.addView(icon, 0)

                    (icon.layoutParams as? LinearLayout.LayoutParams)?.let { layoutParams ->
                        layoutParams.gravity = Gravity.CENTER_VERTICAL
                    }
                }
            }
        }
    }

}
package net.dankito.deepthought.android.service.communication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.data_access.network.communication.callback.ShouldPermitSynchronizingWithDeviceCallback
import net.dankito.data_access.network.communication.message.DeviceInfo
import net.dankito.deepthought.android.MainActivity


class AndroidIsSynchronizationPermittedHandler(private var context: Context) : IsSynchronizationPermittedHandler {

    companion object {

        val IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME = "IsSynchronizationPermittedHandler"

        val SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION = "ShouldPermitSynchronizingWithDevice"

        val SHOW_CORRECT_RESPONSE_TO_USER_NON_BLOCKING_ACTION = "ShowCorrectResponseToUserNonBlocking"

        val DEVICE_INFO_EXTRA_NAME = "DeviceInfo"

        val CORRECT_RESPONSE_EXTRA_NAME = "CorrectResponse"

        val PERMITS_SYNCHRONIZATION_EXTRA_NAME = "PermitsSynchronization"

    }


    override fun shouldPermitSynchronizingWithDevice(remoteDeviceInfo: DeviceInfo, callback: ShouldPermitSynchronizingWithDeviceCallback) {
        createBroadcastReceiverForShouldPermitSynchronizingWithDeviceResultIntent(remoteDeviceInfo, callback)

        val callMainActivityIntent = Intent(context, MainActivity::class.java)
        callMainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        callMainActivityIntent.putExtra(IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME, SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION)

        callMainActivityIntent.putExtra(DEVICE_INFO_EXTRA_NAME, remoteDeviceInfo.toString())

        context.startActivity(callMainActivityIntent)
    }

    protected fun createBroadcastReceiverForShouldPermitSynchronizingWithDeviceResultIntent(remoteDeviceInfo: DeviceInfo, callback: ShouldPermitSynchronizingWithDeviceCallback) {
        val filter = IntentFilter()
        filter.addAction(SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                context.unregisterReceiver(this)
                handleShouldPermitSynchronizingWithDeviceResultIntent(remoteDeviceInfo, intent, callback)
            }
        }

        context.registerReceiver(receiver, filter)
    }

    protected fun handleShouldPermitSynchronizingWithDeviceResultIntent(remoteDeviceInfo: DeviceInfo, intent: Intent, callback: ShouldPermitSynchronizingWithDeviceCallback) {
        val permitsSynchronization = intent.getBooleanExtra(PERMITS_SYNCHRONIZATION_EXTRA_NAME, false)

        callback.done(remoteDeviceInfo, permitsSynchronization)
    }


    override fun showCorrectResponseToUserNonBlocking(remoteDeviceInfo: DeviceInfo, correctResponse: String) {
        val callMainActivityIntent = Intent(context, MainActivity::class.java)
        callMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        callMainActivityIntent.putExtra(IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME, SHOW_CORRECT_RESPONSE_TO_USER_NON_BLOCKING_ACTION)

        callMainActivityIntent.putExtra(DEVICE_INFO_EXTRA_NAME, remoteDeviceInfo.toString())
        callMainActivityIntent.putExtra(CORRECT_RESPONSE_EXTRA_NAME, correctResponse)

        context.startActivity(callMainActivityIntent)
    }

}

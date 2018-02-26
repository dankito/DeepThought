package net.dankito.deepthought.android.dialogs

import android.app.Activity
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import net.dankito.deepthought.android.R
import net.dankito.deepthought.android.service.CurrentActivityTracker
import net.dankito.deepthought.android.service.showKeyboardDelayed
import net.dankito.util.localization.Localization
import net.dankito.util.ui.dialog.IDialogService
import net.dankito.util.ui.dialog.ConfirmationDialogButton
import net.dankito.util.ui.dialog.ConfirmationDialogConfig


class AndroidDialogService(private val currentActivityTracker: CurrentActivityTracker, private val localizationProperty: Localization) : IDialogService {

    override fun getLocalization(): Localization {
        return localizationProperty
    }


    override fun showLittleInfoMessage(infoMessage: CharSequence) {
        currentActivityTracker.currentActivity?.let { currentActivity ->
            currentActivity.runOnUiThread { showLittleInfoMessageOnUIThread(currentActivity, infoMessage) }
        }
    }

    private fun showLittleInfoMessageOnUIThread(activity: Activity, infoMessage: CharSequence) {
        Toast.makeText(activity, infoMessage, Toast.LENGTH_LONG).show()
    }

    override fun showInfoMessage(infoMessage: CharSequence, alertTitle: CharSequence?) {
        currentActivityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { showInfoMessageOnUIThread(activity, infoMessage, alertTitle) }
        }
    }

    private fun showInfoMessageOnUIThread(activity: Activity, message: CharSequence, alertTitle: CharSequence?) {
        val builder = createDialog(activity, message, alertTitle, android.R.drawable.ic_dialog_info)

        buildAndShowDialog(builder)
    }

    override fun showConfirmationDialog(message: CharSequence, alertTitle: CharSequence?, config: ConfirmationDialogConfig, optionSelected: (ConfirmationDialogButton) -> Unit) {
        currentActivityTracker.currentActivity?.let { activity ->
            if(Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showConfirmMessageOnUiThread(activity, message, alertTitle, config, optionSelected)
            }
            else {
                activity.runOnUiThread { showConfirmMessageOnUiThread(activity, message, alertTitle, config, optionSelected) }
            }
        }
    }

    private fun showConfirmMessageOnUiThread(activity: Activity, message: CharSequence, alertTitle: CharSequence?, config: ConfirmationDialogConfig, optionSelected: (ConfirmationDialogButton) -> Unit) {
        val builder = createDialog(activity, message, alertTitle)

        if(config.showNoButton) {
            builder.setNegativeButton(getConfirmButtonText(activity, R.string.action_no, config.noButtonText), { _, _ -> optionSelected(ConfirmationDialogButton.No) })
        }
        else {
            builder.setNegativeButton(null, null)
        }

        if(config.showThirdButton) {
            builder.setNeutralButton(getConfirmButtonText(activity, R.string.action_cancel, config.thirdButtonText), { _, _ -> optionSelected(ConfirmationDialogButton.ThirdButton) })
        }

        val confirmButtonText = getConfirmButtonText(activity, if(config.showNoButton) R.string.action_yes else R.string.action_ok, config.confirmButtonText)
        builder.setPositiveButton(confirmButtonText, { _, _ -> optionSelected(ConfirmationDialogButton.Confirm) })

        buildAndShowDialog(builder)
    }

    private fun getConfirmButtonText(activity: Activity, defaultTextResourceId: Int, customText: String?): String {
        customText?.let { return it }

        return activity.getString(defaultTextResourceId)
    }

    override fun showErrorMessage(errorMessage: CharSequence, alertTitle: CharSequence?, exception: Exception?) {
        currentActivityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { showErrorMessageOnUIThread(activity, errorMessage, alertTitle, exception) }
        }
    }

    private fun showErrorMessageOnUIThread(activity: Activity, errorMessage: CharSequence, alertTitle: CharSequence?, exception: Exception?) {
        var message = errorMessage.toString()
        exception?.let {
            message += ":\r\n\r\n" + exception.localizedMessage
        }

        val builder = createDialog(activity, message, alertTitle, android.R.drawable.ic_dialog_alert)

        buildAndShowDialog(builder)
    }

    override fun askForTextInput(questionText: CharSequence, alertTitleText: CharSequence?, defaultValue: CharSequence?, type: net.dankito.util.ui.dialog.InputType, callback: (Boolean, String?) -> Unit) {
        currentActivityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { askForTextInputOnUIThread(activity, questionText, alertTitleText, defaultValue, type, callback) }
        }
    }

    private fun askForTextInputOnUIThread(activity: Activity, questionText: CharSequence, alertTitleText: CharSequence?, defaultValue: CharSequence?, type: net.dankito.util.ui.dialog.InputType, callback: (Boolean, String?) -> Unit) {
        val builder = createDialog(activity, questionText, alertTitleText)

        val input = EditText(activity)
        input.inputType = if(type == net.dankito.util.ui.dialog.InputType.Numbers) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT
        defaultValue?.let { input.setText(it) }
        builder.setView(input)

        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            val enteredResponse = input.text.toString()
            callback(true, enteredResponse)

            dialog.cancel()
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
            callback(false, null)

            dialog.cancel()
        }

        buildAndShowDialog(builder)

        input.showKeyboardDelayed()
    }



    private fun createDialog(activity: Activity, message: CharSequence, alertTitle: CharSequence?, iconResource: Int? = null): AlertDialog.Builder {
        var builder = AlertDialog.Builder(activity)

        alertTitle?.let { builder = builder.setTitle(it) }

        builder = builder.setMessage(message)

        iconResource?.let { builder.setIcon(it) }

        builder.setNegativeButton(android.R.string.ok, null)

        return builder
    }

    private fun buildAndShowDialog(builder: AlertDialog.Builder) {
        val dialog = builder.create()

        dialog.show()
    }

}
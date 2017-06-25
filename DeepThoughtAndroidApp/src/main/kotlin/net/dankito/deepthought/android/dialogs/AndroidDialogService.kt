package net.dankito.deepthought.android.dialogs

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import net.dankito.deepthought.android.service.ui.CurrentActivityTracker
import net.dankito.deepthought.ui.IDialogService


class AndroidDialogService(private val currentActivityTracker: CurrentActivityTracker) : IDialogService {

    override fun showInfoMessage(infoMessage: String, alertTitle: String?) {
        currentActivityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { showInfoMessageOnUIThread(activity, infoMessage, alertTitle) }
        }
    }

    private fun showInfoMessageOnUIThread(activity: Activity, message: String, alertTitle: String?) {
        val builder = createDialog(activity, message, alertTitle, android.R.drawable.ic_dialog_info)

        builder.create().show()
    }

    override fun showConfirmationDialog(message: String, alertTitle: String?, optionSelected: (Boolean) -> Unit) {
        currentActivityTracker.currentActivity?.let { activity ->
            if(Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showConfirmMessageOnUiThread(activity, message, alertTitle, optionSelected)
            }
            else {
                activity.runOnUiThread { showConfirmMessageOnUiThread(activity, message, alertTitle, optionSelected) }
            }
        }
    }

    private fun showConfirmMessageOnUiThread(activity: Activity, message: String, alertTitle: String?, optionSelected: (Boolean) -> Unit) {
        val builder = createDialog(activity, message, alertTitle)

        builder.setNegativeButton(android.R.string.no, { _, _ -> optionSelected(false) })

        builder.setNegativeButton(android.R.string.yes, { _, _ -> optionSelected(true) })

        builder.create().show()
    }

    override fun showErrorMessage(errorMessage: String, alertTitle: String?, exception: Exception?) {
        currentActivityTracker.currentActivity?.let { activity ->
            activity.runOnUiThread { showErrorMessageOnUIThread(activity, errorMessage, alertTitle, exception) }
        }
    }

    private fun showErrorMessageOnUIThread(activity: Activity, errorMessage: String, alertTitle: String?, exception: Exception?) {
        val builder = createDialog(activity, errorMessage, alertTitle, android.R.drawable.ic_dialog_alert)

        // TODO: show exception

        builder.create().show()
    }

    override fun askForTextInput(questionText: String, alertTitleText: String?, defaultValue: String?, callback: (Boolean, String?) -> Unit) {
        currentActivityTracker.currentActivity?.let { activity ->
            askForTextInputOnUIThread(activity, questionText, alertTitleText, defaultValue, callback)
        }
    }

    private fun askForTextInputOnUIThread(activity: Activity, questionText: String, alertTitleText: String?, defaultValue: String?, callback: (Boolean, String?) -> Unit) {
        val builder = createDialog(activity, questionText, alertTitleText)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
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

        builder.show()

        input.requestFocus()
        input.postDelayed({
            val keyboard = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(input, 0)
        }, 50)
    }



    private fun createDialog(activity: Activity, message: CharSequence, alertTitle: CharSequence?, iconResource: Int? = null): AlertDialog.Builder {
        var builder = AlertDialog.Builder(activity)

        alertTitle?.let { builder = builder.setTitle(it) }

        builder = builder.setMessage(message)

        iconResource?.let { builder.setIcon(it) }

        builder.setNegativeButton(android.R.string.ok, null)

        return builder
    }
}
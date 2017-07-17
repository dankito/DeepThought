package net.dankito.utils.ui


interface IDialogService {

    fun showLittleInfoMessage(infoMessage: CharSequence)

    fun showInfoMessage(infoMessage: CharSequence, alertTitle: CharSequence? = null)

    fun showConfirmationDialog(message: CharSequence, alertTitle: CharSequence? = null, optionSelected: (Boolean) -> Unit)

    fun showErrorMessage(errorMessage: CharSequence, alertTitle: CharSequence? = null, exception: Exception? = null)

    fun askForTextInput(questionText: CharSequence, alertTitleText: CharSequence? = null, defaultValue: CharSequence? = null, type: InputType = InputType.Text,
                        callback: (Boolean, String?) -> Unit)

}
package net.dankito.deepthought.ui


interface IDialogService {

    fun showInfoMessage(infoMessage: String, alertTitle: String? = null)

    fun showConfirmationDialog(message: String, alertTitle: String? = null, optionSelected: (Boolean) -> Unit)

    fun showErrorMessage(errorMessage: String, alertTitle: String? = null, exception: Exception? = null)

    fun askForTextInput(questionText: String, alertTitleText: String? = null, defaultValue: String? = null, callback: (Boolean, String?) -> Unit)

}
package net.dankito.deepthought.ui


interface IDialogService {

    fun showInfoMessage(infoMessage: String, alertTitle: String?)

    fun showConfirmationDialog(message: String, alertTitle: String?, optionSelected: (Boolean) -> Unit)

    fun showErrorMessage(errorMessage: String, alertTitle: String?, exception: Exception?)

    fun askForTextInput(questionText: String, alertTitleText: String?, defaultValue: String?, callback: (Boolean, String?) -> Unit)

}
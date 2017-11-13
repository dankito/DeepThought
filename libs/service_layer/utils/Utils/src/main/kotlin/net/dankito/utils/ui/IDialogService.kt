package net.dankito.utils.ui

import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig


interface IDialogService {

    fun showLittleInfoMessage(infoMessage: CharSequence)

    fun showInfoMessage(infoMessage: CharSequence, alertTitle: CharSequence? = null)

    fun showConfirmationDialog(message: CharSequence, alertTitle: CharSequence? = null, config: ConfirmationDialogConfig = ConfirmationDialogConfig(), optionSelected: (ConfirmationDialogButton) -> Unit)

    fun showErrorMessage(errorMessage: CharSequence, alertTitle: CharSequence? = null, exception: Exception? = null)

    fun askForTextInput(questionText: CharSequence, alertTitleText: CharSequence? = null, defaultValue: CharSequence? = null, type: InputType = InputType.Text,
                        callback: (Boolean, String?) -> Unit)


    fun getLocalization(): Localization // so that users of IDialogService don't need to get Localization as well

}
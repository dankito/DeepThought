package net.dankito.deepthought.javafx.dialogs

import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.utils.localization.Localization
import net.dankito.utils.ui.IDialogService
import net.dankito.utils.ui.InputType
import net.dankito.utils.ui.model.ConfirmationDialogButton
import net.dankito.utils.ui.model.ConfirmationDialogConfig
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI
import java.net.URLEncoder
import kotlin.concurrent.thread


class JavaFXDialogService(private val localizationProperty: Localization, private val showSendBugReportButton: Boolean = false,
                          private val emailAddressForBugReports: String? = null, private val subjectForBugReports: String? = null) : IDialogService {

    companion object {
        private val log = LoggerFactory.getLogger(JavaFXDialogService::class.java)
    }


    override fun getLocalization(): Localization {
        return localizationProperty
    }


    override fun showLittleInfoMessage(infoMessage: CharSequence) {
        // nothing to do for JavaFX, was only introduced for showing Android Toasts
    }

    override fun showInfoMessage(infoMessage: CharSequence, alertTitle: CharSequence?) {
        showInfoMessage(infoMessage, alertTitle, null)
    }

    fun showInfoMessage(infoMessage: CharSequence, alertTitle: CharSequence?, owner: Stage?) {
        FXUtils.runOnUiThread { showInfoMessageOnUiThread(infoMessage, alertTitle, owner) }
    }

    private fun showInfoMessageOnUiThread(infoMessage: CharSequence, alertTitle: CharSequence?, owner: Stage?) {
        val alert = createDialog(Alert.AlertType.INFORMATION, infoMessage, alertTitle, owner, ButtonType.OK)

        alert.showAndWait()
    }


    override fun showConfirmationDialog(message: CharSequence, alertTitle: CharSequence?, config: ConfirmationDialogConfig, optionSelected: (ConfirmationDialogButton) -> Unit) {
        showConfirmationDialog(message, alertTitle, config, null, optionSelected)
    }

    fun showConfirmationDialog(message: CharSequence, alertTitle: CharSequence?, config: ConfirmationDialogConfig = ConfirmationDialogConfig(), owner: Stage?, optionSelected: (ConfirmationDialogButton) -> Unit) {
        FXUtils.runOnUiThread { showConfirmationDialogOnUiThread(message, alertTitle, config, owner, optionSelected) }
    }

    private fun showConfirmationDialogOnUiThread(message: CharSequence, alertTitle: CharSequence?, config: ConfirmationDialogConfig, owner: Stage?, optionSelected: (ConfirmationDialogButton) -> Unit) {
        val buttons = ArrayList<ButtonType>()
        if(config.showNoButton) {
            buttons.add(ButtonType.NO)
        }
        if(config.showThirdButton) {
            buttons.add(ButtonType.CANCEL) // TODO: test
        }
        buttons.add(ButtonType.YES)

        val alert = createDialog(Alert.AlertType.CONFIRMATION, message, alertTitle, owner, *buttons.toTypedArray())

        when(alert.showAndWait().get()) {
            ButtonType.NO -> return optionSelected(ConfirmationDialogButton.No)
            else -> return optionSelected(ConfirmationDialogButton.Confirm)
        }
    }


    override fun showErrorMessage(errorMessage: CharSequence, alertTitle: CharSequence?, exception: Exception?) {
        showErrorMessage(errorMessage, alertTitle, exception, null)
    }

    fun showErrorMessage(errorMessage: CharSequence, alertTitle: CharSequence?, exception: Exception?, owner: Stage?) {
        FXUtils.runOnUiThread { showErrorMessageOnUiThread(errorMessage, alertTitle, exception, owner) }
    }

    private fun showErrorMessageOnUiThread(errorMessage: CharSequence, alertTitle: CharSequence?, exception: Exception?, owner: Stage?) {
        val alert = createDialog(Alert.AlertType.ERROR, errorMessage, alertTitle, owner, ButtonType.OK)
        alert.isResizable = true

        if(exception != null) {
            createExpandableException(alert, exception)
        }

        val clickedButton = alert.showAndWait().get()

        if(clickedButton?.buttonData == ButtonBar.ButtonData.OTHER && exception != null) {
            sendBugReport(exception, errorMessage)
        }
    }

    private fun createExpandableException(alert: Alert, exception: Exception) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val exceptionText = sw.toString()

        val label = Label(localizationProperty.getLocalizedString("dialog.service.error.stacktrace.label"))

        val textArea = TextArea(exceptionText)
        textArea.isEditable = false
        textArea.isWrapText = true

        textArea.maxWidth = FXUtils.SizeMaxValue
        textArea.maxHeight = FXUtils.SizeMaxValue
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val expContent = GridPane()
        expContent.maxWidth = FXUtils.SizeMaxValue
        expContent.add(label, 0, 0)
        expContent.add(textArea, 0, 1)

        if(showSendBugReportButton) {
            val sendBugReportButton = ButtonType(localizationProperty.getLocalizedString("alert.message.send.bug.report"), ButtonBar.ButtonData.OTHER)
            alert.buttonTypes.add(sendBugReportButton)
        }

        // Set expandable Exception into the dialog pane.
        alert.dialogPane.expandableContent = expContent
    }

    private fun sendBugReport(exception: Exception, errorMessage: CharSequence) {
        if(emailAddressForBugReports != null) {
            thread { // get off UI thread
                try {
                    val mailUri = createMailUriForBugReport(errorMessage, exception)

                    Desktop.getDesktop().mail(URI.create(mailUri))
                } catch(e: Exception) { log.error("Could not send bug report", e) }
            }
        }
    }

    private fun createMailUriForBugReport(errorMessage: CharSequence, exception: Exception): String {
        var bugReport = errorMessage.toString() + "\r\n\r\n"

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val stackTrace = sw.toString()
        pw.close()

        bugReport += "Stack trace:\r\n\r\n$stackTrace"

        var mailUri = "mailto:$emailAddressForBugReports?"

        subjectForBugReports?.let {
            mailUri += "subject=${URLEncoder.encode(it, "utf-8").replace("+", "%20")}&" // use %20, not URLEncoder's + as Thunderbird understands only %20
        }

        val encodedBugReport = URLEncoder.encode(bugReport, "utf-8").replace("+", "%20")
        mailUri += "body=$encodedBugReport"

        return mailUri
    }


    private fun createDialog(alertType: Alert.AlertType, message: CharSequence, alertTitle: CharSequence?, owner: Stage?, vararg buttons: ButtonType): Alert {
        val alert = Alert(alertType)

        (alertTitle as? String)?.let { alert.title = it }

        owner?.let { alert.initOwner(it) }

        (message as? String)?.let { setAlertContent(alert, it) }
        alert.headerText = null

        alert.buttonTypes.setAll(*buttons)

        return alert
    }

    private fun setAlertContent(alert: Alert, content: String) {
        var maxWidth = Screen.getPrimary().visualBounds.width

        if(alert.owner != null) {
            FXUtils.getScreenWindowLeftUpperCornerIsIn(alert.owner)?.let { ownersScreen ->
                maxWidth = ownersScreen.visualBounds.width
            }
        }

        maxWidth *= 0.6 // set max width to 60 % of Screen width

        val contentLabel = Label(content)
        contentLabel.isWrapText = true
        contentLabel.prefHeight = Region.USE_COMPUTED_SIZE
        contentLabel.maxHeight = FXUtils.SizeMaxValue
        contentLabel.maxWidth = maxWidth

        val contentPane = VBox(contentLabel)
        contentPane.prefHeight = Region.USE_COMPUTED_SIZE
        contentPane.maxHeight = FXUtils.SizeMaxValue
        VBox.setVgrow(contentLabel, Priority.ALWAYS)

        alert.dialogPane.prefHeight = Region.USE_COMPUTED_SIZE
        alert.dialogPane.maxHeight = FXUtils.SizeMaxValue
        alert.dialogPane.maxWidth = maxWidth
        alert.dialogPane.content = contentPane
    }


    override fun askForTextInput(questionText: CharSequence, alertTitleText: CharSequence?, defaultValue: CharSequence?, type: InputType, callback: (Boolean, String?) -> Unit) {
        FXUtils.runOnUiThread { askForTextInputOnUiThread(questionText, alertTitleText, defaultValue, callback) }
    }

    private fun askForTextInputOnUiThread(questionText: CharSequence, alertTitleText: CharSequence?, defaultValue: CharSequence?, callback: (Boolean, String?) -> Unit) {
        val dialog = TextInputDialog(defaultValue as? String)
        dialog.headerText = null
        dialog.title = alertTitleText as? String
        dialog.contentText = questionText as? String

        val result = dialog.showAndWait()

        val enteredText = if(result.isPresent) result.get() else null
        callback(result.isPresent, enteredText)
    }

}
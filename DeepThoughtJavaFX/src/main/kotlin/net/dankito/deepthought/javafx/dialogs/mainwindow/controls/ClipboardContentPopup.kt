package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import net.dankito.deepthought.javafx.res.Colors
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.utils.clipboard.ClipboardContentOption
import net.dankito.utils.clipboard.OptionsForClipboardContent
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*


class ClipboardContentPopup(clipboardWatcher: JavaFXClipboardWatcher) : View() {

    private val isPopupVisible = SimpleBooleanProperty(false)

    private val isPopupEnabled = SimpleBooleanProperty(true)

    private val headerText = SimpleStringProperty("")

    private val isActionExecuting = SimpleBooleanProperty(false)
    private val actionProgress = SimpleStringProperty("")


    private var optionsPane: VBox by singleAssign()


    init {
        clipboardWatcher.addClipboardContentChangedExternallyListener {
            runLater {
                if(isPopupEnabled.value) { // clipboard content changed -> if popup is still active (= no action has been selected), hide it
                    isPopupVisible.value = false
                }
            }
        }
    }


    override val root = vbox {
        visibleProperty().bind(isPopupVisible)
        disableProperty().bind(isPopupEnabled.not())
        ensureOnlyUsesSpaceIfVisible()

        background = Background(BackgroundFill(Colors.ClipboardContentPopupBackgroundColor, CornerRadii(8.0), Insets.EMPTY))

        anchorpane {
            label(headerText) {
                minWidth = 150.0

                anchorpaneConstraints {
                    topAnchor = 8.0
                    leftAnchor = 10.0
                    rightAnchor = 30.0 // to not overlap with close button
                    bottomAnchor = 4.0
                }
            }

            button("x") {
                action { hidePopupOnUiThread() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 4.0
                }
            }
        }

        hbox {
            vbox {
                hboxConstraints {
                    alignment = Pos.CENTER
                    marginLeft = 4.0
                    marginRight = 4.0
                }

                progressindicator {
                    maxWidth = 30.0
                    maxHeight = maxWidth
                    alignment = Pos.CENTER
                    visibleProperty().bind(isActionExecuting)

                    vboxConstraints {
                        marginBottom = 4.0
                    }
                }

                label(actionProgress) {
                    prefWidth = 50.0
                    alignment = Pos.CENTER
                    textAlignment = TextAlignment.CENTER
                    visibleProperty().bind(isActionExecuting)
                }

            }

            optionsPane = vbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER_LEFT
            }
        }
    }


    fun setClipboardContentOptions(options: OptionsForClipboardContent) {
        isPopupVisible.value = true
        isPopupEnabled.value = true
        headerText.value = options.headerTitle
        optionsPane.children.clear()

        options.options.forEachIndexed { index, option ->
            addOption(option, getKeyCombinationForOption(index), { option.callAction() })
        }
    }

    private fun addOption(option: ClipboardContentOption, keyCombination: KeyCodeCombination? = null, optionSelected: () -> Unit) {
        val displayedText = option.title + (if(keyCombination == null) "" else " (${keyCombination.displayText})")

        val optionLink = Button(displayedText)
        optionLink.isUnderline = true
        optionLink.textFill = Color.BLACK
        optionLink.background = Background.EMPTY
        optionLink.cursor = Cursor.HAND
        optionLink.prefHeight = 24.0

        optionsPane.add(optionLink)
        VBox.setMargin(optionLink, Insets(0.0, 10.0, 6.0, 0.0))

        optionLink.setOnAction { optionSelected() }
        keyCombination?.let { optionLink.scene?.accelerators?.put(it, Runnable { optionSelected() }) }

        optionLink.setOnMouseEntered { optionLink.background = Background(BackgroundFill(Colors.ClipboardContentPopupOptionMouseOverColor, CornerRadii(8.0), Insets.EMPTY)) }
        optionLink.setOnMouseExited { optionLink.background = Background.EMPTY }


        option.addIsExecutingListener { progress ->
            runLater {
                isActionExecuting.value = option.isExecuting
                isPopupEnabled.value = ! isActionExecuting.value
                isPopupVisible.value = isActionExecuting.value
                actionProgress.value = option.progressString
            }
        }
    }

    private fun getKeyCombinationForOption(index: Int): KeyCodeCombination? {
        return when(index) {
            0 -> KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN)
            1 -> KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN)
            2 -> KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            3 -> KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN)
            else -> null
        }
    }


    private fun showPopupOnUiThread() {
        isPopupVisible.value = true
    }

    private fun hidePopupOnUiThread() {
        isPopupVisible.value = false
    }

}
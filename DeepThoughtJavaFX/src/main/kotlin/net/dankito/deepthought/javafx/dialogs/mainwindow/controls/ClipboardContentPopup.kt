package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.res.Colors
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardContent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.javafx.util.FXUtils
import net.dankito.deepthought.service.clipboard.ClipboardContentOption
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContent
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContentDetector
import tornadofx.*
import javax.inject.Inject


class ClipboardContentPopup : View() {

    private val isPopupVisible = SimpleBooleanProperty(false)

    private val isPopupEnabled = SimpleBooleanProperty(true)

    private val headerText = SimpleStringProperty("")


    @Inject
    protected lateinit var clipboardWatcher: JavaFXClipboardWatcher

    @Inject
    protected lateinit var optionsDetector: OptionsForClipboardContentDetector


    private var optionsPane: VBox by singleAssign()


    init {
        AppComponent.component.inject(this)

        clipboardWatcher.addClipboardContentChangedExternallyListener { clipboardContentChangedExternally(it) }
    }


    override val root = vbox {
        visibleProperty().bind(isPopupVisible)
        disableProperty().bind(isPopupEnabled.not())
        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

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

        optionsPane = vbox { }
    }


    private fun clipboardContentChangedExternally(clipboardContent: JavaFXClipboardContent) {
        isPopupVisible.value = false
        headerText.value = ""
        optionsPane.children.clear()

        optionsDetector.getOptionsAsync(clipboardContent) { options ->
            runLater {
                retrievedOptionsOnUiThread(options)
            }
        }
    }

    private fun retrievedOptionsOnUiThread(options: OptionsForClipboardContent) {
        isPopupVisible.value = true
        isPopupEnabled.value = true
        headerText.value = options.headerTitle

        options.options.forEachIndexed { index, option ->
            addOption(option, getKeyCombinationForOption(index), { option.callAction() })
        }
    }

    private fun addOption(option: ClipboardContentOption, keyCombination: KeyCodeCombination? = null, optionSelected: () -> Unit) {
        val displayedText = option.title + (if(keyCombination == null) "" else " (${keyCombination.displayText})")

        val isActionExecuting = SimpleBooleanProperty(false)
        val actionProgress = SimpleStringProperty("0 %")

        val optionLayout = hbox {
            cursor = Cursor.HAND
            prefHeight = 24.0

            progressindicator {
                prefWidth = 20.0
                alignment = Pos.CENTER_LEFT
                visibleProperty().bind(isActionExecuting)

                hboxConstraints {
                    marginLeft = 4.0
                    marginRight = 4.0
                }
            }

            label(actionProgress) {
                prefWidth = 50.0
                alignment = Pos.CENTER_RIGHT
                visibleProperty().bind(isActionExecuting)
            }

            button(displayedText) {
                isUnderline = true
                textFill = Color.BLACK
                background = Background.EMPTY

                action { optionSelected()  }
                keyCombination?.let { scene?.accelerators?.put(it, Runnable { optionSelected() }) }

                setOnMouseEntered { background = Background(BackgroundFill(Colors.ClipboardContentPopupOptionMouseOverColor, CornerRadii(8.0), Insets.EMPTY)) }
                setOnMouseExited { background = Background.EMPTY }
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }

        optionsPane.add(optionLayout)


        option.addIsExecutingListener { progress ->
            runLater {
                isActionExecuting.value = progress >= 0.0 && progress < 100.0
                isPopupEnabled.value = ! isActionExecuting.value
                isPopupVisible.value = isActionExecuting.value
                actionProgress.value = String.format("%.1f", progress) + " %"
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
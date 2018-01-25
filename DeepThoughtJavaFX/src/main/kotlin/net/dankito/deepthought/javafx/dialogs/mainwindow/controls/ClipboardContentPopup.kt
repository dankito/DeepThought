package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.Button
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
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.UrlUtil
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import javax.inject.Inject


class ClipboardContentPopup() : View() {

    private val isPopupVisible = SimpleBooleanProperty(false)

    private val headerText = SimpleStringProperty("")


    @Inject
    protected lateinit var clipboardWatcher: JavaFXClipboardWatcher

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var urlUtil: UrlUtil


    private var optionsPane: VBox by singleAssign()


    init {
        AppComponent.component.inject(this)

        clipboardWatcher.addClipboardContentChangedExternallyListener { clipboardContentChangedExternally(it) }
    }


    override val root = anchorpane {
        useMaxSize = true
        visibleProperty().bind(isPopupVisible)
        FXUtils.ensureNodeOnlyUsesSpaceIfVisible(this)

        vbox {
            background = Background(BackgroundFill(Colors.ClipboardContentPopupBackgroundColor, CornerRadii(8.0), Insets.EMPTY))

            anchorpaneConstraints {
                rightAnchor = 8.0
                bottomAnchor = 8.0
            }

            anchorpane {
                label(headerText) {
                    minWidth = 150.0

                    anchorpaneConstraints {
                        topAnchor = 8.0
                        leftAnchor = 10.0
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
    }


    private fun clipboardContentChangedExternally(clipboardContent: JavaFXClipboardContent) {
        isPopupVisible.value = clipboardContent.hasUrl()
        headerText.value = ""
        optionsPane.children.clear()

        clipboardContent.url?.let { url ->
            headerText.value = String.format(messages["clipboard.content.header.create.item.from"], urlUtil.getHostName(url))

            addOption(FX.messages["clipboard.content.option.try.to.extract.important.web.page.parts"],
                    KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN)) { extractItemFromUrl(url)}

            addOption(FX.messages["clipboard.content.option.extract.plain.text.only"],
                    KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)) { } // TODO
        }
    }

    private fun addOption(optionText: String, accelerator: KeyCodeCombination? = null, optionSelected: () -> Unit) {
        val displayedText = optionText + (if(accelerator == null) "" else " ($accelerator)")

        val optionLink = Button(displayedText)
        optionLink.isUnderline = true
        optionLink.textFill = Color.BLACK
        optionLink.background = Background.EMPTY
        optionLink.cursor = Cursor.HAND
        optionLink.prefHeight = 24.0

        optionsPane.add(optionLink)
        VBox.setMargin(optionLink, Insets(0.0, 10.0, 6.0, 18.0))

        optionLink.setOnAction { optionLinkPressed(optionSelected) }
        accelerator?.let { optionLink.scene?.accelerators?.put(it, Runnable { optionLinkPressed(optionSelected) }) }

        optionLink.setOnMouseEntered { optionLink.background = Background(BackgroundFill(Colors.ClipboardContentPopupOptionMouseOverColor, CornerRadii(8.0), Insets.EMPTY)) }
        optionLink.setOnMouseExited { optionLink.background = Background.EMPTY }
    }

    private fun optionLinkPressed(optionSelected: () -> Unit) {
        hidePopupOnUiThread()
        optionSelected()
    }


    private fun extractItemFromUrl(url: String) {
        articleExtractorManager.extractArticleUserDidNotSeeBeforeAndAddDefaultDataAsync(url) {
            it.result?.let { router.showEditEntryView(it) }
            it.error?.let { showErrorMessage(it, url) }
        }
    }

    private fun showErrorMessage(error: Exception, articleUrl: String) {
        dialogService.showErrorMessage(dialogService.getLocalization().getLocalizedString("alert.message.could.not.extract.item.from.url", articleUrl), exception = error)
    }


    private fun showPopupOnUiThread() {
        isPopupVisible.value = true
    }

    private fun hidePopupOnUiThread() {
        isPopupVisible.value = false
    }

}
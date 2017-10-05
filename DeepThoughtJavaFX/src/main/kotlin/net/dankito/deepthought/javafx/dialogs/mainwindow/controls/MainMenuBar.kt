package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.value.ChangeListener
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardContent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.news.article.ArticleExtractorManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.utils.UrlUtil
import net.dankito.utils.ui.IDialogService
import tornadofx.*
import javax.inject.Inject


class MainMenuBar : View() {

    @Inject
    protected lateinit var articleExtractorManager: ArticleExtractorManager

    @Inject
    protected lateinit var dialogService: IDialogService

    @Inject
    protected lateinit var router: IRouter

    @Inject
    protected lateinit var urlUtil: UrlUtil


    private lateinit var clipboardWatcher: JavaFXClipboardWatcher

    private lateinit var mnitmFileClipboard: Menu


    init {
        AppComponent.component.inject(this)
    }

    override val root = gridpane {
        row {
            menubar {
                minHeight = 30.0
                maxHeight = 30.0

                gridpaneColumnConstraints {
                    hgrow = Priority.ALWAYS
                    minWidth = 50.0
                }

                menu(messages["main.window.menu.file"]) {
                    mnitmFileClipboard = menu(messages["main.window.menu.clipboard"])

                    separator()

                    item(messages["main.window.menu.quit"], KeyCombination.keyCombination("Ctrl+Q")) {
                        action { primaryStage.close() }
                    }
                }

                menu(messages["main.window.menu.view"]) {
                    isVisible = false
                    checkmenuitem(messages["main.window.menu.view.show.quick.edit.entry.pane"], KeyCombination.keyCombination("F4")) {
                        selectedProperty().addListener(ChangeListener<Boolean> { observable, oldValue, newValue ->

                        })
                    }
                }

                menu(messages["main.window.menu.tools"]) {
                    isVisible = false
                    menu(messages["main.window.menu.tools.language"]) {
                        checkmenuitem(messages["application.language.english"]) {
                            action { }
                        }
                        checkmenuitem(messages["application.language.german"]) {
                            action { }
                        }
                    }
                }

                menu(messages["main.window.menu.window"]) {
                    isVisible = false
                }

                menu(messages["main.window.menu.help"]) {
                    isVisible = false
                    item(messages["main.window.menu.help.about"]) {
                        action { }
                    }
                }
            }

            add(ArticleExtractorsMenuButton())
        }

        clipboardWatcher = JavaFXClipboardWatcher(primaryStage, urlUtil) { clipboardContentChangedExternally(it) }
    }


    private fun clipboardContentChangedExternally(clipboardContent: JavaFXClipboardContent) {
        mnitmFileClipboard.isDisable = !clipboardContent.hasUrl()

        mnitmFileClipboard.items.clear()

        clipboardContent.url?.let { url ->
            val extractContentFromUrlMenuItem = MenuItem(messages["main.window.menu.file.extract.entry.from.url"])
            extractContentFromUrlMenuItem.action { extractEntryFromUrl(url) }
            mnitmFileClipboard.items.add(extractContentFromUrlMenuItem)
        }
    }

    private fun extractEntryFromUrl(url: String) {
        articleExtractorManager.extractArticleAndAddDefaultDataAsync(url) {
            it.result?.let { router.showEditEntryView(it) }
            it.error?.let { showErrorMessage(it, url) }
        }
    }

    private fun showErrorMessage(error: Exception, articleUrl: String) {
        dialogService.showErrorMessage(dialogService.getLocalization().getLocalizedString("alert.message.could.not.extract.entry.from.url", articleUrl), exception = error)
    }

}
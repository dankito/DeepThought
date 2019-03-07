package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.beans.value.ChangeListener
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardContent
import net.dankito.deepthought.javafx.service.clipboard.JavaFXClipboardWatcher
import net.dankito.deepthought.javafx.service.import_export.DataImporterExporterManager
import net.dankito.deepthought.service.clipboard.OptionsForClipboardContent
import net.dankito.deepthought.service.data.DataManager
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.search.IIndexBasedSearchEngine
import net.dankito.service.search.ISearchEngine
import net.dankito.service.search.specific.ItemsSearch
import net.dankito.utils.extensions.sortedByStrings
import tornadofx.*
import java.io.File
import javax.inject.Inject


class MainMenuBar(private val dataManager: DataManager) : View() {

    @Inject
    protected lateinit var importerExporterManager: DataImporterExporterManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var clipboardWatcher: JavaFXClipboardWatcher

    @Inject
    protected lateinit var router: IRouter


    var createNewItemMenuClicked: (() -> Unit)? = null


    private lateinit var mnitmFileClipboard: Menu

    private lateinit var mnitmFileImport: Menu

    private lateinit var mnitmFileExport: Menu


    private var isDataManagerInitialized = false

    private var isUiInitialized = false


    init {
        dataManager.addInitializationListener {
            AppComponent.component.inject(this)

            isDataManagerInitialized = true

            clipboardWatcher.addClipboardContentChangedExternallyListener { clipboardContentChangedExternally(it) }
            clipboardWatcher.addClipboardOptionsChangedListener { clipboardContentOptionsChanged(it) }

            addImporterAndExporterIfInitialized()
        }
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
                    setOnShowing { fileMenuIsAboutToShow() }

                    menu(messages["main.window.menu.file.new"]) {
                        item(messages["main.window.menu.file.new.new.item"], KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)) {
                            action { createNewItemMenuClicked?.invoke() }
                        }

                        item(messages["main.window.menu.file.new.new.item.from.pdf"], KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)) {
                            action { createItemFromPdfFile() }
                        }
                    }

                    mnitmFileClipboard = menu(messages["main.window.menu.file.clipboard"]) {
                        isDisable = true
                    }

                    separator()

                    mnitmFileImport = menu(messages["main.window.menu.file.import"])

                    mnitmFileExport = menu(messages["main.window.menu.file.export"])

                    separator()

                    item(messages["main.window.menu.tools.rebuild.search.index"]) {
                        action { (searchEngine as? IIndexBasedSearchEngine)?.rebuildIndexAsync() }
                    }

                    separator()

                    item(messages["main.window.menu.file.quit"], KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)) {
                        action { primaryStage.close() }
                    }
                }

                menu(messages["main.window.menu.view"]) {
                    isVisible = false
                    checkmenuitem(messages["main.window.menu.view.show.quick.edit.item.pane"], KeyCombination.keyCombination("F4")) {
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

        isUiInitialized = true

        addImporterAndExporterIfInitialized()
    }


    private fun createItemFromPdfFile() {
        router.createItemFromPdf()
    }


    private fun addImporterAndExporterIfInitialized() {
        if (isDataManagerInitialized && isUiInitialized) {
            addImporterAndExporter()
        }
    }

    private fun addImporterAndExporter() {
        importerExporterManager.importer.sortedByStrings { it.name }.forEach { importer ->
            mnitmFileImport.item(importer.name) {
                action { getFileToImport()?.let { importer.importAsync(it) { } } }
            }
        }

        importerExporterManager.exporter.sortedByStrings { it.name }.forEach { exporter ->
            mnitmFileExport.item(exporter.name) {
                action { getFileToExportTo()?.let { file ->
                    searchEngine.searchItems(ItemsSearch {
                        exporter.exportAsync(file, it)
                    })
                } }
            }
        }
    }

    private fun getFileToImport(): File? {
        return selectFileToOpen("main.window.menu.file.select.file.to.import")
    }

    private fun getFileToExportTo(): File? {
        val chooser = FileChooser()
        chooser.title = messages["main.window.menu.file.select.file.to.export"]
        return chooser.showSaveDialog(root.scene.window)
    }

    private fun fileMenuIsAboutToShow() {

    }


    private fun clipboardContentChangedExternally(clipboardContent: JavaFXClipboardContent) {
        mnitmFileClipboard.isDisable = true

        mnitmFileClipboard.items.clear()
    }

    private fun clipboardContentOptionsChanged(options: OptionsForClipboardContent) {
        mnitmFileClipboard.isDisable = false

        options.options.forEach { option ->
            val optionMenuItem = MenuItem(option.title)
            optionMenuItem.action { option.callAction() }
            mnitmFileClipboard.items.add(optionMenuItem)
        }
    }


    private fun selectFileToOpen(titleResourceKey: String? = null, vararg extensionFilter: FileChooser.ExtensionFilter): File? {
        val chooser = FileChooser()

        titleResourceKey?.let {
            chooser.title = messages[it]
        }

        chooser.extensionFilters.addAll(*extensionFilter)

        return chooser.showOpenDialog(root.scene.window)
    }

}
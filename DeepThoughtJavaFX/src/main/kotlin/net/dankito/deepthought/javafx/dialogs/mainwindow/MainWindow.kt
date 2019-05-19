package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.application.Platform
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.*
import net.dankito.deepthought.service.data.DataManager
import net.dankito.utils.javafx.ui.extensions.setAnchorPaneOverallAnchor
import net.dankito.utils.windowregistry.window.WindowRegistry
import net.dankito.utils.windowregistry.window.javafx.ui.JavaFXMainWindow
import tornadofx.*
import tornadofx.FX.Companion.messages
import javax.inject.Inject


class MainWindow : JavaFXMainWindow(String.format(messages["main.window.title"], getAppVersion())) {

    companion object {

        private fun getAppVersion(): String {
            val javaPackage = MainWindow::class.java.getPackage()
            if (javaPackage != null && javaPackage.implementationVersion != null) {
                return javaPackage.implementationVersion
            }
            else {
                return "Develop"
            }
        }

    }


    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var injectedWindowRegistry: WindowRegistry


    private var stckpnContent: StackPane by singleAssign()

    private var splpnContent: SplitPane by singleAssign()

    private var mainMenuBar: MainMenuBar by singleAssign()

    val tagsListView: TagsListView by inject()

    val sourcesListView: SourcesListView by inject()

    val itemsListView: ItemsListView by inject()

    val statusBar: StatusBar by inject()


    init {
        setupUI()
    }


    override fun setupDependencyInjection() {
        super.setupDependencyInjection()

        AppComponent.component.inject(this)
    }

    override fun getWindowRegistryInstance(): WindowRegistry {
        return injectedWindowRegistry
    }


    override fun onUndock() {
        super.onUndock()

        Platform.exit()
    }


    override val root = borderpane {
        prefHeight = 620.0
        prefWidth = 1150.0

        mainMenuBar = MainMenuBar(dataManager)
        top = mainMenuBar.root

        center {
            stckpnContent = stackpane {
                splpnContent = splitpane {
                    tabpane {
                        prefWidth = 300.0
                        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                        tab(messages["tab.tags.label"]) {
                            add(tagsListView.root)

                            selectedProperty().addListener { _, _, newValue -> if(newValue) tagsListView.viewCameIntoView() }
                        }

                        tab(messages["tab.sources.label"]) {
                            add(sourcesListView.root)

                            selectedProperty().addListener { _, _, newValue -> if(newValue) sourcesListView.viewCameIntoView() }
                        }
                    }

                    anchorpane {
                        itemsListView.statusBar = statusBar
                        add(itemsListView)
                        itemsListView.setAnchorPaneOverallAnchor(0.0)

                        addCMessagePopupPane(this)
                    }
                }
            }

            splpnContent.setDividerPosition(0, 0.2)
        }

        bottom = statusBar.root

        mainMenuBar.createNewItemMenuClicked = { itemsListView.createNewItem() }
    }

    private fun addCMessagePopupPane(pane: AnchorPane) {
        val messagePopupPane = MessagePopupPane(dataManager)
        pane.add(messagePopupPane)

        AnchorPane.setRightAnchor(messagePopupPane.root, 8.0)
        AnchorPane.setBottomAnchor(messagePopupPane.root, 8.0)
    }


    private fun setupUI() {
        setStageIcon(Image(MainWindow::class.java.classLoader.getResourceAsStream("icons/AppIcon.png")))
    }


    override val windowDataClass = null

    override fun getCurrentWindowData() = null

}
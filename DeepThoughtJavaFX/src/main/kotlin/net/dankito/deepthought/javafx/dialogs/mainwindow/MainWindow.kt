package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.*
import net.dankito.deepthought.javafx.service.extensions.setAnchorPaneOverallAnchor
import net.dankito.deepthought.service.data.DataManager
import tornadofx.*
import tornadofx.FX.Companion.messages
import javax.inject.Inject


class MainWindow : View(String.format(messages["main.window.title"], getAppVersion())) {

    companion object {

        private fun getAppVersion(): String {
            val javaPackage = javaClass.getPackage()
            if(javaPackage != null) {
                return javaPackage.implementationVersion
            }
            else {
                return "Develop"
            }
        }

    }


    @Inject
    protected lateinit var dataManager: DataManager


    private var stckpnContent: StackPane by singleAssign()

    private var splpnContent: SplitPane by singleAssign()

    private var mainMenuBar: MainMenuBar by singleAssign()

    val tagsListView: TagsListView by inject()

    val sourcesListView: SourcesListView by inject()

    val itemsListView: ItemsListView by inject()

    val statusBar: StatusBar by inject()


    init {
        AppComponent.component.inject(this)

        setupUI()
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

                            selectedProperty().addListener { _, _, newValue -> if(newValue) tagsListView.showItemsForLastSelectedEntity() }
                        }

                        tab(messages["tab.sources.label"]) {
                            add(sourcesListView.root)

                            selectedProperty().addListener { _, _, newValue -> if(newValue) sourcesListView.showItemsForLastSelectedEntity() }
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

}
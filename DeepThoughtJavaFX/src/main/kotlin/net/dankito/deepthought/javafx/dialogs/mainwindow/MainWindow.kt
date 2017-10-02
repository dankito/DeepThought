package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.EntriesListView
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.MainMenuBar
import net.dankito.deepthought.javafx.dialogs.mainwindow.controls.TagsListView
import tornadofx.*


class MainWindow : View() {

    val controller: MainWindowController by inject()


    override val root: BorderPane by fxml()

    private var tbpnOverview: TabPane by singleAssign()

    private var tabTags: Tab by singleAssign()

    private var splpnContent: SplitPane by singleAssign()

    private var contentPane: VBox by singleAssign()

    val mainMenuBar: MainMenuBar by inject()

    val tagsListView: TagsListView by inject()

    val entriesListView: EntriesListView by inject()


    init {
        AppComponent.component.inject(this)

        setupUI()
    }

    private fun setupUI() {
        splpnContent = splitpane {
            tbpnOverview = tabpane {
                prefWidth = 300.0
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                tabTags = tab(messages["tags.tab.label"]) {
                    prefWidth = 300.0

                    add(tagsListView.root)
                }
            }

            contentPane = vbox {

            }
        }

        root.center = splpnContent

        contentPane.add(entriesListView.root)
        VBox.setVgrow(entriesListView.root, Priority.ALWAYS)

        splpnContent.setDividerPosition(0, 0.2)

        root.top = mainMenuBar.root
    }

}
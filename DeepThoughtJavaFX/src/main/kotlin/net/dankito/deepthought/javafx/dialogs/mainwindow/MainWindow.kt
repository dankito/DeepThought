package net.dankito.deepthought.javafx.dialogs.mainwindow

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.MenuButton
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import tornadofx.*


class MainWindow : View() {

    val controller: MainWindowController by inject()


    override val root: BorderPane by fxml()

    val tbpnOverview: TabPane by fxid()

    val tabTags: Tab by fxid()

    val btnOnlineArticleExtractors: MenuButton by fxid()


    fun handleMenuItemFileCloseAction(event: ActionEvent) {
        primaryStage.close()
    }


    @FXML
    fun handleMenuItemToolsBackupsAction(event: Event) {

    }

    @FXML
    fun handleMainMenuWindowShowing(event: Event) {

    }

    protected var checkMenuItemViewShowQuickEditEntrySelectedChangeListener: ChangeListener<Boolean> = object : ChangeListener<Boolean> {
        override fun changed(observable: ObservableValue<out Boolean>, oldValue: Boolean?, newValue: Boolean?) {

        }
    }

}
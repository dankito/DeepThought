package net.dankito.deepthought.javafx

import javafx.application.Application
import javafx.stage.Stage
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import tornadofx.*


class DeepThoughtJavaFXApplication : App(MainWindow::class) {

    val mainWindowController: MainWindowController by inject()


    override fun start(stage: Stage) {
        super.start(stage)
        mainWindowController.init()
    }

}



fun main(args: Array<String>) {
    Application.launch(DeepThoughtJavaFXApplication::class.java, *args)
}
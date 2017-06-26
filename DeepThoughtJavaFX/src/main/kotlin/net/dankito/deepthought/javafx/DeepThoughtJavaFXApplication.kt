package net.dankito.deepthought.javafx

import javafx.application.Application
import javafx.stage.Stage
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.di.DaggerAppComponent
import net.dankito.deepthought.javafx.di.JavaFXModule
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import tornadofx.*
import javax.inject.Inject


class DeepThoughtJavaFXApplication : App(MainWindow::class) {

    @Inject
    protected lateinit var appInitializer: JavaFXAppInitializer


    val mainWindowController: MainWindowController by inject()


    override fun start(stage: Stage) {
        setupDI() // has to be called before Stage is created as creates FX.messages

        super.start(stage)

        mainWindowController.init()
    }


    private fun setupDI() {
        val component = DaggerAppComponent.builder()
                .javaFXModule(JavaFXModule(mainWindowController))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.component = component

        // DataManager currently initializes itself, so inject DataManager here so that it start asynchronously initializing itself in parallel to creating UI and therefore
        // speeding app start up a bit.
        // That's also the reason why LuceneSearchEngine gets injected here so that as soon as DataManager is initialized it can initialize its indices
        component.inject(this)

        appInitializer.initializeApp()
    }


    @Throws(Exception::class)
    override fun stop() {
        super.stop()
        System.exit(0) // otherwise Window would be closed but application still running in background
    }

}



fun main(args: Array<String>) {
    Application.launch(DeepThoughtJavaFXApplication::class.java, *args)
}
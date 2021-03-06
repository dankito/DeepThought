package net.dankito.deepthought.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import net.dankito.deepthought.di.BaseComponent
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.javafx.appstart.JavaFXAppInitializer
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.di.DaggerAppComponent
import net.dankito.deepthought.javafx.di.JavaFXInstanceProvider
import net.dankito.deepthought.javafx.di.JavaFXModule
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.utils.localization.Localization
import tornadofx.*
import java.util.*
import javax.inject.Inject


open class DeepThoughtJavaFXApplication : App(MainWindow::class) {

    @Inject
    protected lateinit var appInitializer: JavaFXAppInitializer

    @Inject
    protected lateinit var localization: Localization


    private val mainWindowController: MainWindowController by inject()


    override fun start(stage: Stage) {
        setupDI(stage)

        setupMessagesResources() // has to be done before creating / injecting first instances as some of them already rely on Messages (e.g. CalculatedTags)

        stage.setOnCloseRequest { Platform.exit() } // stop application as otherwise all other windows would stay open

        appInitializer.initializeApp()

        super.start(stage)
    }


    private fun setupMessagesResources() {
        ResourceBundle.clearCache() // at this point default ResourceBundles are already created and cached. In order that ResourceBundle created below takes effect cache has to be clearedbefore

        FX.messages = localization.messagesResourceBundle
    }

    private fun setupDI(primaryStage: Stage) {
        val component = DaggerAppComponent.builder()
                .javaFXModule(JavaFXModule(primaryStage, createFlavorInstanceProvider(), mainWindowController))
                .build()

        BaseComponent.component = component
        CommonComponent.component = component
        AppComponent.component = component

        component.inject(this)
    }

    protected open fun createFlavorInstanceProvider(): JavaFXInstanceProvider {
        return JavaFXInstanceProvider()
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
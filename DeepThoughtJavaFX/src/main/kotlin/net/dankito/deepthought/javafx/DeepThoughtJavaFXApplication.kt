package net.dankito.deepthought.javafx

import javafx.application.Application
import javafx.stage.Stage
import net.dankito.deepthought.di.CommonComponent
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.di.DaggerAppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindow
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.service.data.DataManager
import net.dankito.service.search.ISearchEngine
import tornadofx.*
import javax.inject.Inject


class DeepThoughtJavaFXApplication : App(MainWindow::class) {

    @Inject
    protected lateinit var dataManager: DataManager

    @Inject
    protected lateinit var searchEngine: ISearchEngine


    val mainWindowController: MainWindowController by inject()


    override fun start(stage: Stage) {
        super.start(stage)

        setupDI()

        mainWindowController.init()
    }


    private fun setupDI() {
        val component = DaggerAppComponent.builder()
                .build()

        CommonComponent.component = component
        AppComponent.component = component

        // DataManager currently initializes itself, so inject DataManager here so that it start asynchronously initializing itself in parallel to creating UI and therefore
        // speeding app start up a bit.
        // That's also the reason why LuceneSearchEngine gets injected here so that as soon as DataManager is initialized it can initialize its indices
        component.inject(this)
    }

}



fun main(args: Array<String>) {
    Application.launch(DeepThoughtJavaFXApplication::class.java, *args)
}
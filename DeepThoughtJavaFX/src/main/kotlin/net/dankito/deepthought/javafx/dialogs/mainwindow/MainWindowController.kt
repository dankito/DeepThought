package net.dankito.deepthought.javafx.dialogs.mainwindow

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.EntryService
import tornadofx.*
import javax.inject.Inject


class MainWindowController : Controller() {


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var router: IRouter


    fun init() {
        AppComponent.component.inject(this)
    }


}
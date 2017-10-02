package net.dankito.deepthought.javafx.dialogs.mainwindow

import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.model.EntryViewModel
import net.dankito.deepthought.javafx.util.LazyLoadingObservableList
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.service.data.EntryService
import tornadofx.*
import javax.inject.Inject


class MainWindowController : Controller() {


    val entryModel = EntryViewModel()

    val entries = LazyLoadingObservableList<Entry>()


    @Inject
    protected lateinit var entryService: EntryService

    @Inject
    protected lateinit var router: IRouter


    fun init() {
        AppComponent.component.inject(this)
    }


}
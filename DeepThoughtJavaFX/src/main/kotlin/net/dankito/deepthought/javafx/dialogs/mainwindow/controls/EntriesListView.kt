package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.referencePreview
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.ui.IRouter
import net.dankito.deepthought.ui.presenter.EntriesListPresenter
import net.dankito.deepthought.ui.view.IEntriesListView
import net.dankito.service.search.ISearchEngine
import tornadofx.*
import java.text.DateFormat
import javax.inject.Inject


class EntriesListView : View(), IEntriesListView {

    companion object {
        private val dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG)
    }

    val controller: MainWindowController by inject()

    private val entriesListPresenter: EntriesListPresenter


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    init {
        AppComponent.component.inject(this)

        entriesListPresenter = EntriesListPresenter(this, router, searchEngine)
    }

    override fun onUndock() {
        entriesListPresenter.cleanUp()
        super.onUndock()
    }


    override val root = tableview<Entry> {
        column("index", Entry::entryIndex).weigthedWidth(1.0)
        column("reference", Entry::referencePreview).weigthedWidth(4.0)
        column("content", Entry::entryPreview).weigthedWidth(4.0)
//        column("createdOn", stringBinding(Entry::createdOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)
//        column("modifiedOn", stringBinding(Entry::modifiedOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)

        columnResizePolicy = SmartResize.POLICY

        items = controller.entries

        bindSelected(controller.entryModel)

        onDoubleClick { router.showEditEntryView(selectionModel.selectedItem) }
    }


    /*          IEntriesListView implementation            */

    override fun showEntries(entries: List<Entry>) {
        runLater { controller.entries.setAll(entries) }
    }

}
package net.dankito.deepthought.javafx.dialogs.mainwindow.controls

import javafx.scene.layout.Priority
import net.dankito.deepthought.extensions.entryPreview
import net.dankito.deepthought.extensions.referencePreview
import net.dankito.deepthought.javafx.di.AppComponent
import net.dankito.deepthought.javafx.dialogs.mainwindow.MainWindowController
import net.dankito.deepthought.javafx.routing.JavaFXRouter
import net.dankito.deepthought.model.Entry
import net.dankito.deepthought.model.Tag
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

    private val controller: MainWindowController by inject()

    private val searchBar: EntriesSearchBar

    private val presenter: EntriesListPresenter


    @Inject
    protected lateinit var searchEngine: ISearchEngine

    @Inject
    protected lateinit var router: IRouter

    init {
        AppComponent.component.inject(this)

        presenter = EntriesListPresenter(this, router, searchEngine)
        searchBar = EntriesSearchBar(presenter)

        (router as? JavaFXRouter)?.entriesListView = this // TODO: this is bad code design
    }

    override fun onUndock() {
        presenter.cleanUp()
        super.onUndock()
    }


    override val root = vbox {
        add(searchBar.root)

        tableview<Entry>(controller.entries) {
            column(messages["entry.column.header.index"], Entry::entryIndex).prefWidth(46.0)
            column(messages["entry.column.header.reference"], Entry::referencePreview).weigthedWidth(4.0)
            column(messages["entry.column.header.preview"], Entry::entryPreview).weigthedWidth(4.0)
    //        column(messages["entry.column.header.created"], stringBinding(Entry::createdOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)
    //        column(messages["entry.column.header.modified"], stringBinding(Entry::modifiedOn) { dateTimeFormat.format(this) }).weigthedWidth(1.0)

            columnResizePolicy = SmartResize.POLICY

            bindSelected(controller.entryModel)

            vgrow = Priority.ALWAYS

            onDoubleClick { router.showEditEntryView(selectionModel.selectedItem) }
        }
    }


    /*          IEntriesListView implementation            */

    override fun showEntries(entries: List<Entry>) {
        runLater { controller.entries.setAll(entries) }
    }

    override fun showEntriesForTag(tag: Tag, entries: List<Entry>) {
        presenter.showEntriesForTag(tag, entries)
    }

}